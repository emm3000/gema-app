package com.emm.gema.core.domain.attendance

import com.emm.gema.core.domain.fake.InMemoryAttendanceRepository
import com.emm.gema.core.domain.fake.InMemoryStudentRepository
import com.emm.gema.core.domain.student.Student
import com.google.common.truth.Truth.assertThat
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneOffset
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

private val today: LocalDate = LocalDate.of(2026, 9, 10)
private val yesterday: LocalDate = today.minusDays(1)
private val tomorrow: LocalDate = today.plusDays(1)

class AttendanceUseCasesTest {

    private val luz: Student = student("student-1", "12345678901234", "ACOSTA RIVERA, Luz Maria")
    private val jose: Student = student("student-2", "12345678901235", "BAUTISTA QUISPE, Jose")

    private val studentRepository = InMemoryStudentRepository(listOf(jose, luz))
    private val attendanceRepository = InMemoryAttendanceRepository()
    private val clock: Clock = Clock.fixed(today.atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneOffset.UTC)

    private val getAttendanceDay = GetAttendanceDayUseCase(studentRepository, attendanceRepository)
    private val recordAttendance = RecordAttendanceUseCase(attendanceRepository, clock)
    private val countAttendanceDays = CountAttendanceDaysUseCase(attendanceRepository)

    @Test
    fun `an untouched day shows every student as present and stores nothing`() = runTest {
        val entries: List<AttendanceEntry> = getAttendanceDay(sectionId, today).first()

        assertThat(entries.map { it.student.fullName })
            .containsExactly("ACOSTA RIVERA, Luz Maria", "BAUTISTA QUISPE, Jose").inOrder()
        assertThat(entries.map { it.status }).containsExactly(AttendanceStatus.PRESENT, AttendanceStatus.PRESENT)
        assertThat(entries.map { it.isRecorded }).containsExactly(false, false)
        assertThat(attendanceRepository.records.value).isEmpty()
    }

    @Test
    fun `each of the four statuses is recorded for one student and one date`() = runTest {
        AttendanceStatus.entries.forEach { status: AttendanceStatus ->
            recordAttendance(sectionId, luz.id, today, status)

            val entry: AttendanceEntry = getAttendanceDay(sectionId, today).first().single { it.student.id == luz.id }
            assertThat(entry.status).isEqualTo(status)
            assertThat(entry.isRecorded).isTrue()
        }

        assertThat(attendanceRepository.records.value).hasSize(1)
    }

    @Test
    fun `recording one student leaves the others on the unrecorded default`() = runTest {
        recordAttendance(sectionId, luz.id, today, AttendanceStatus.ABSENT)

        val entries: List<AttendanceEntry> = getAttendanceDay(sectionId, today).first()

        assertThat(entries.single { it.student.id == jose.id }.isRecorded).isFalse()
        assertThat(entries.single { it.student.id == jose.id }.status).isEqualTo(AttendanceStatus.PRESENT)
    }

    @Test
    fun `a past date is recorded and read back on its own date`() = runTest {
        recordAttendance(sectionId, luz.id, yesterday, AttendanceStatus.LATE)

        assertThat(getAttendanceDay(sectionId, yesterday).first().single { it.student.id == luz.id }.status)
            .isEqualTo(AttendanceStatus.LATE)
        assertThat(getAttendanceDay(sectionId, today).first().single { it.student.id == luz.id }.isRecorded)
            .isFalse()
    }

    @Test
    fun `a future date is refused`() = runTest {
        val outcome: Result<Unit> = runCatching {
            recordAttendance(sectionId, luz.id, tomorrow, AttendanceStatus.PRESENT)
        }

        assertThat(outcome.exceptionOrNull()).isInstanceOf(IllegalArgumentException::class.java)
        assertThat(attendanceRepository.records.value).isEmpty()
    }

    @Test
    fun `a withdrawn student is listed before the withdrawal date and gone from it on`() = runTest {
        studentRepository.save(jose.copy(withdrawalDate = today))

        assertThat(getAttendanceDay(sectionId, yesterday).first().map { it.student.id })
            .containsExactly(luz.id, jose.id)
        assertThat(getAttendanceDay(sectionId, today).first().map { it.student.id }).containsExactly(luz.id)
    }

    @Test
    fun `a withdrawn student keeps the records taken before the withdrawal`() = runTest {
        recordAttendance(sectionId, jose.id, yesterday, AttendanceStatus.JUSTIFIED)

        studentRepository.save(jose.copy(withdrawalDate = today))

        assertThat(getAttendanceDay(sectionId, yesterday).first().single { it.student.id == jose.id }.status)
            .isEqualTo(AttendanceStatus.JUSTIFIED)
    }

    @Test
    fun `a day nobody touched summarises as untaken`() = runTest {
        val summary: AttendanceDaySummary = getAttendanceDay(sectionId, today).first().summarise()

        assertThat(summary.isTaken).isFalse()
        assertThat(summary.presentCount).isEqualTo(2)
        assertThat(summary.totalCount).isEqualTo(2)
        assertThat(summary.unmarkedCount).isEqualTo(2)
    }

    @Test
    fun `one recorded student makes the day taken`() = runTest {
        recordAttendance(sectionId, luz.id, today, AttendanceStatus.ABSENT)

        val summary: AttendanceDaySummary = getAttendanceDay(sectionId, today).first().summarise()

        assertThat(summary.isTaken).isTrue()
        assertThat(summary.presentCount).isEqualTo(1)
        assertThat(summary.unmarkedCount).isEqualTo(1)
    }

    @Test
    fun `recorded days are counted once per date`() = runTest {
        assertThat(countAttendanceDays(sectionId)).isEqualTo(0)

        recordAttendance(sectionId, luz.id, today, AttendanceStatus.ABSENT)
        recordAttendance(sectionId, jose.id, today, AttendanceStatus.LATE)
        recordAttendance(sectionId, luz.id, yesterday, AttendanceStatus.ABSENT)

        assertThat(countAttendanceDays(sectionId)).isEqualTo(2)
    }
}
