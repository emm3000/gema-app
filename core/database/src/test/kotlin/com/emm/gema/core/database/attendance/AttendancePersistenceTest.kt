package com.emm.gema.core.database.attendance

import com.emm.gema.core.database.GemaDb
import com.emm.gema.core.database.UuidIdGenerator
import com.emm.gema.core.database.curriculum.SqlDelightWorkedCompetencyRepository
import com.emm.gema.core.database.evaluation.SqlDelightPeriodLevelRepository
import com.emm.gema.core.database.siagie.SqlDelightSiagieImportStore
import com.emm.gema.core.database.inMemoryGemaDb
import com.emm.gema.core.database.section.SqlDelightSectionAreaRepository
import com.emm.gema.core.database.section.SqlDelightSectionRepository
import com.emm.gema.core.database.student.SqlDelightStudentRepository
import com.emm.gema.core.domain.attendance.AttendanceEntry
import com.emm.gema.core.domain.attendance.AttendanceRepository
import com.emm.gema.core.domain.attendance.AttendanceStatus
import com.emm.gema.core.domain.attendance.CountAttendanceDaysUseCase
import com.emm.gema.core.domain.attendance.GetAttendanceDayUseCase
import com.emm.gema.core.domain.attendance.GetMonthlyAttendanceSummaryUseCase
import com.emm.gema.core.domain.attendance.MonthlyAttendanceSummary
import com.emm.gema.core.domain.attendance.RecordAttendanceUseCase
import com.emm.gema.core.domain.attendance.StudentAttendanceMonthCount
import com.emm.gema.core.domain.curriculum.WorkedCompetencyRepository
import com.emm.gema.core.domain.section.CreateSectionUseCase
import com.emm.gema.core.domain.section.DeleteSectionUseCase
import com.emm.gema.core.domain.section.Grade
import com.emm.gema.core.domain.section.Section
import com.emm.gema.core.domain.section.SectionAreaRepository
import com.emm.gema.core.domain.section.SectionRepository
import com.emm.gema.core.domain.student.SaveStudentUseCase
import com.emm.gema.core.domain.student.Student
import com.emm.gema.core.domain.student.StudentRepository
import com.emm.gema.core.domain.student.StudentSaveResult
import com.emm.gema.core.domain.student.WithdrawStudentUseCase
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.time.Clock
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneOffset

private const val firstCode: String = "12345678901234"
private const val secondCode: String = "12345678901235"
private val today: LocalDate = LocalDate.of(2026, 9, 10)
private val yesterday: LocalDate = today.minusDays(1)

@OptIn(ExperimentalCoroutinesApi::class)
class AttendancePersistenceTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val database: GemaDb = inMemoryGemaDb()
    private val sectionRepository: SectionRepository = SqlDelightSectionRepository(database, dispatcher)
    private val sectionAreaRepository: SectionAreaRepository = SqlDelightSectionAreaRepository(database, dispatcher)
    private val studentRepository: StudentRepository = SqlDelightStudentRepository(database, dispatcher)
    private val attendanceRepository: AttendanceRepository = SqlDelightAttendanceRepository(database, dispatcher)
    private val workedCompetencyRepository: WorkedCompetencyRepository =
        SqlDelightWorkedCompetencyRepository(database, dispatcher)

    private val clock: Clock = Clock.fixed(today.atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneOffset.UTC)
    private val createSection = CreateSectionUseCase(sectionRepository, UuidIdGenerator())
    private val saveStudent = SaveStudentUseCase(studentRepository, UuidIdGenerator())
    private val withdrawStudent = WithdrawStudentUseCase(studentRepository)
    private val getAttendanceDay = GetAttendanceDayUseCase(studentRepository, attendanceRepository)
    private val recordAttendance = RecordAttendanceUseCase(attendanceRepository, clock)
    private val countAttendanceDays = CountAttendanceDaysUseCase(attendanceRepository)
    private val getMonthlySummary = GetMonthlyAttendanceSummaryUseCase(studentRepository, attendanceRepository)
    private val deleteSection = DeleteSectionUseCase(
        sectionRepository,
        sectionAreaRepository,
        workedCompetencyRepository,
        studentRepository,
        SqlDelightSiagieImportStore(database, dispatcher),
        SqlDelightPeriodLevelRepository(database, dispatcher),
        attendanceRepository,
    )

    @Test
    fun `a status survives a read back on its own date`() = runTest {
        val section: Section = section()
        val student: Student = savedStudent(section.id, firstCode, "ACOSTA RIVERA, Luz Maria")

        recordAttendance(section.id, student.id, today, AttendanceStatus.LATE)

        val entry: AttendanceEntry = getAttendanceDay(section.id, today).first().single()
        assertThat(entry.status).isEqualTo(AttendanceStatus.LATE)
        assertThat(entry.isRecorded).isTrue()
    }

    @Test
    fun `a second tap on the same student and date replaces the stored status`() = runTest {
        val section: Section = section()
        val student: Student = savedStudent(section.id, firstCode, "ACOSTA RIVERA, Luz Maria")

        recordAttendance(section.id, student.id, today, AttendanceStatus.ABSENT)
        recordAttendance(section.id, student.id, today, AttendanceStatus.JUSTIFIED)

        assertThat(getAttendanceDay(section.id, today).first().single().status)
            .isEqualTo(AttendanceStatus.JUSTIFIED)
        assertThat(countAttendanceDays(section.id)).isEqualTo(1)
    }

    @Test
    fun `a withdrawn student leaves the list but keeps its earlier records`() = runTest {
        val section: Section = section()
        val student: Student = savedStudent(section.id, firstCode, "ACOSTA RIVERA, Luz Maria")
        savedStudent(section.id, secondCode, "BAUTISTA QUISPE, Jose")

        recordAttendance(section.id, student.id, yesterday, AttendanceStatus.ABSENT)
        withdrawStudent(student.id, today)

        assertThat(getAttendanceDay(section.id, today).first().map { it.student.id }).doesNotContain(student.id)
        assertThat(getAttendanceDay(section.id, yesterday).first().single { it.student.id == student.id }.status)
            .isEqualTo(AttendanceStatus.ABSENT)
    }

    @Test
    fun `recorded days are counted per section`() = runTest {
        val section: Section = section()
        val other: Section = createSection(section.schoolYearId, Grade.FOURTH, "B")
        val student: Student = savedStudent(section.id, firstCode, "ACOSTA RIVERA, Luz Maria")

        recordAttendance(section.id, student.id, today, AttendanceStatus.PRESENT)
        recordAttendance(section.id, student.id, yesterday, AttendanceStatus.PRESENT)

        assertThat(countAttendanceDays(section.id)).isEqualTo(2)
        assertThat(countAttendanceDays(other.id)).isEqualTo(0)
    }

    @Test
    fun `a deleted section takes its attendance with it`() = runTest {
        val section: Section = section()
        val student: Student = savedStudent(section.id, firstCode, "ACOSTA RIVERA, Luz Maria")
        recordAttendance(section.id, student.id, today, AttendanceStatus.ABSENT)

        deleteSection(section.id)

        assertThat(countAttendanceDays(section.id)).isEqualTo(0)
    }

    @Test
    fun `a month's summary counts survive a read back scoped to that month`() = runTest {
        val section: Section = section()
        val student: Student = savedStudent(section.id, firstCode, "ACOSTA RIVERA, Luz Maria")

        recordAttendance(section.id, student.id, today, AttendanceStatus.LATE)
        recordAttendance(section.id, student.id, LocalDate.of(2026, 8, 20), AttendanceStatus.ABSENT)

        val summary: MonthlyAttendanceSummary = getMonthlySummary(section.id, YearMonth.from(today)).first()

        assertThat(summary.recordedDayCount).isEqualTo(1)
        val counts: StudentAttendanceMonthCount = summary.rows.single { it.studentId == student.id }
        assertThat(counts.countsByStatus[AttendanceStatus.LATE]).isEqualTo(1)
        assertThat(counts.countsByStatus[AttendanceStatus.ABSENT]).isEqualTo(0)
    }

    private suspend fun section(): Section = createSection("2026", Grade.THIRD, "A")

    private suspend fun savedStudent(sectionId: String, code: String, fullName: String): Student {
        val result: StudentSaveResult = saveStudent(sectionId, null, code, fullName)
        return (result as StudentSaveResult.Saved).student
    }
}
