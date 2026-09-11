package com.emm.gema.core.database.section

import com.emm.gema.core.database.GemaDb
import com.emm.gema.core.database.UuidIdGenerator
import com.emm.gema.core.database.activity.SqlDelightActivityRepository
import com.emm.gema.core.database.activity.SqlDelightEvidenceLevelRepository
import com.emm.gema.core.database.attendance.SqlDelightAttendanceRepository
import com.emm.gema.core.database.curriculum.SqlDelightWorkedCompetencyRepository
import com.emm.gema.core.database.evaluation.SqlDelightPeriodLevelRepository
import com.emm.gema.core.database.inMemoryGemaDb
import com.emm.gema.core.database.siagie.SqlDelightSiagieImportStore
import com.emm.gema.core.database.student.SqlDelightStudentRepository
import com.emm.gema.core.domain.activity.Activity
import com.emm.gema.core.domain.activity.ActivityRepository
import com.emm.gema.core.domain.activity.EvidenceLevel
import com.emm.gema.core.domain.activity.EvidenceLevelKey
import com.emm.gema.core.domain.activity.EvidenceLevelRepository
import com.emm.gema.core.domain.attendance.AttendanceRecord
import com.emm.gema.core.domain.attendance.AttendanceRepository
import com.emm.gema.core.domain.attendance.AttendanceStatus
import com.emm.gema.core.domain.curriculum.WorkedCompetencyRepository
import com.emm.gema.core.domain.evaluation.AchievementLevel
import com.emm.gema.core.domain.evaluation.PeriodLevel
import com.emm.gema.core.domain.evaluation.PeriodLevelKey
import com.emm.gema.core.domain.evaluation.PeriodLevelRepository
import com.emm.gema.core.domain.section.Area
import com.emm.gema.core.domain.section.CreateSectionUseCase
import com.emm.gema.core.domain.section.Grade
import com.emm.gema.core.domain.section.Section
import com.emm.gema.core.domain.section.SectionAreaRepository
import com.emm.gema.core.domain.section.SectionRepository
import com.emm.gema.core.domain.siagie.ImportedTemplate
import com.emm.gema.core.domain.siagie.ImportedTemplateKind
import com.emm.gema.core.domain.siagie.SiagieImportStore
import com.emm.gema.core.domain.student.Student
import com.emm.gema.core.domain.student.StudentCode
import com.emm.gema.core.domain.student.StudentRepository
import com.google.common.truth.Truth.assertThat
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test

private const val PERIOD_ID: String = "period-1"
private const val COMPETENCY_ID: String = "competency-1"

@OptIn(ExperimentalCoroutinesApi::class)
class SectionCascadeTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val database: GemaDb = inMemoryGemaDb()
    private val sectionRepository: SectionRepository = SqlDelightSectionRepository(database, dispatcher)
    private val sectionAreaRepository: SectionAreaRepository = SqlDelightSectionAreaRepository(database, dispatcher)
    private val workedCompetencyRepository: WorkedCompetencyRepository =
        SqlDelightWorkedCompetencyRepository(database, dispatcher)
    private val studentRepository: StudentRepository = SqlDelightStudentRepository(database, dispatcher)
    private val siagieImportStore: SiagieImportStore = SqlDelightSiagieImportStore(database, dispatcher)
    private val periodLevelRepository: PeriodLevelRepository = SqlDelightPeriodLevelRepository(database, dispatcher)
    private val attendanceRepository: AttendanceRepository = SqlDelightAttendanceRepository(database, dispatcher)
    private val activityRepository: ActivityRepository = SqlDelightActivityRepository(database, dispatcher)
    private val evidenceLevelRepository: EvidenceLevelRepository =
        SqlDelightEvidenceLevelRepository(database, dispatcher)

    private val createSection = CreateSectionUseCase(sectionRepository, UuidIdGenerator())
    private val cascade = SqlDelightSectionCascade(database, dispatcher)

    @Test
    fun `deleting a section clears every child table and the section itself`() = runTest {
        val section: Section = createSection("2026", Grade.THIRD, "A")
        val student = Student(
            id = "student-1",
            sectionId = section.id,
            code = StudentCode("12345678901234"),
            fullName = "ACOSTA RIVERA, Luz Maria",
        )
        studentRepository.save(student)
        sectionAreaRepository.setAreaHidden(section.id, Area.EFIS, isHidden = true)
        workedCompetencyRepository.setWorked(section.id, PERIOD_ID, COMPETENCY_ID, isWorked = true)
        siagieImportStore.apply(
            emptyList(),
            ImportedTemplate(
                sectionId = section.id,
                kind = ImportedTemplateKind.GRADES,
                fileName = "3 Primaria EBR.xlsx",
                content = byteArrayOf(1, 2, 3),
                importedAt = Instant.parse("2026-09-10T12:00:00Z"),
            ),
        )
        periodLevelRepository.save(
            PeriodLevel(
                key = PeriodLevelKey(section.id, PERIOD_ID, student.id, COMPETENCY_ID),
                achievementLevel = AchievementLevel.A,
            ),
        )
        attendanceRepository.record(
            AttendanceRecord(section.id, student.id, LocalDate.of(2026, 9, 10), AttendanceStatus.PRESENT),
        )
        val activity = Activity(
            id = "activity-1",
            sectionId = section.id,
            periodId = PERIOD_ID,
            name = "Feria de talentos",
            date = LocalDate.of(2026, 9, 10),
            competencyIds = setOf(COMPETENCY_ID),
        )
        activityRepository.save(activity)
        evidenceLevelRepository.save(
            EvidenceLevel(
                key = EvidenceLevelKey(activity.id, student.id, COMPETENCY_ID),
                achievementLevel = AchievementLevel.A,
            ),
        )

        cascade.deleteSection(section.id)

        assertThat(sectionRepository.findById(section.id)).isNull()
        assertThat(sectionAreaRepository.observeHiddenAreas(section.id).first()).isEmpty()
        assertThat(workedCompetencyRepository.observeWorked(section.id, PERIOD_ID).first()).isEmpty()
        assertThat(studentRepository.observeBySection(section.id).first()).isEmpty()
        assertThat(siagieImportStore.findTemplate(section.id, ImportedTemplateKind.GRADES)).isNull()
        assertThat(periodLevelRepository.observeByPeriod(section.id, PERIOD_ID).first()).isEmpty()
        assertThat(attendanceRepository.observeBySectionAndDate(section.id, LocalDate.of(2026, 9, 10)).first())
            .isEmpty()
        assertThat(activityRepository.observeByPeriod(section.id, PERIOD_ID).first()).isEmpty()
        assertThat(evidenceLevelRepository.observeByActivity(activity.id).first()).isEmpty()
    }
}
