package com.emm.gema.feature.activities.list

import com.emm.gema.core.domain.activity.Activity
import com.emm.gema.core.domain.activity.ActivityId
import com.emm.gema.core.domain.activity.EvidenceLevel
import com.emm.gema.core.domain.activity.EvidenceLevelKey
import com.emm.gema.core.domain.activity.GetActivitiesUseCase
import com.emm.gema.core.domain.activity.GetActivityEvidenceStudentCountsUseCase
import com.emm.gema.core.domain.curriculum.Competency
import com.emm.gema.core.domain.curriculum.GetWorkedCompetenciesUseCase
import com.emm.gema.core.domain.evaluation.AchievementLevel
import com.emm.gema.core.domain.schoolyear.GetCurrentPeriodUseCase
import com.emm.gema.core.domain.schoolyear.GetPeriodsUseCase
import com.emm.gema.core.domain.schoolyear.GetSchoolYearUseCase
import com.emm.gema.core.domain.schoolyear.Period
import com.emm.gema.core.domain.schoolyear.PeriodId
import com.emm.gema.core.domain.schoolyear.PeriodKind
import com.emm.gema.core.domain.schoolyear.SchoolYear
import com.emm.gema.core.domain.schoolyear.SchoolYearId
import com.emm.gema.core.domain.section.Area
import com.emm.gema.core.domain.section.GetSectionUseCase
import com.emm.gema.core.domain.section.Grade
import com.emm.gema.core.domain.section.Section
import com.emm.gema.core.domain.section.SectionId
import com.emm.gema.core.domain.student.GetStudentsUseCase
import com.emm.gema.core.domain.student.Student
import com.emm.gema.core.domain.student.StudentCode
import com.emm.gema.core.domain.student.StudentId
import com.emm.gema.feature.activities.FakeActivityRepository
import com.emm.gema.feature.activities.FakeCompetencyRepository
import com.emm.gema.feature.activities.FakeEvidenceLevelRepository
import com.emm.gema.feature.activities.FakePeriodRepository
import com.emm.gema.feature.activities.FakeSchoolYearRepository
import com.emm.gema.feature.activities.FakeSectionAreaRepository
import com.emm.gema.feature.activities.FakeSectionRepository
import com.emm.gema.feature.activities.FakeStudentRepository
import com.emm.gema.feature.activities.FakeWorkedCompetencyRepository
import com.emm.gema.feature.activities.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneOffset
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

private val sectionId: SectionId = SectionId("section-1")
private val schoolYearId: SchoolYearId = SchoolYearId("year-1")
private val periodId: PeriodId = PeriodId("period-2")

class ActivitiesViewModelTest {

    @get:Rule
    val mainDispatcherRule: MainDispatcherRule = MainDispatcherRule()

    private val schoolYear: SchoolYear = SchoolYear(
        id = schoolYearId,
        label = "2026",
        startDate = LocalDate.of(2026, 3, 2),
        endDate = LocalDate.of(2026, 12, 18),
        periodKind = PeriodKind.BIMESTER,
    )
    private val section: Section = Section(sectionId, schoolYearId, Grade.THIRD, "A")
    private val period: Period =
        Period(periodId, schoolYearId, 2, LocalDate.of(2026, 5, 11), LocalDate.of(2026, 7, 24))
    private val today: LocalDate = LocalDate.of(2026, 6, 1)
    private val clock: Clock = Clock.fixed(today.atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneOffset.UTC)

    private val activityRepository: FakeActivityRepository = FakeActivityRepository()
    private val evidenceLevelRepository: FakeEvidenceLevelRepository = FakeEvidenceLevelRepository(activityRepository)
    private val workedCompetencyRepository: FakeWorkedCompetencyRepository = FakeWorkedCompetencyRepository()
    private val competencyRepository: FakeCompetencyRepository = FakeCompetencyRepository()
    private val studentRepository: FakeStudentRepository = FakeStudentRepository(
        listOf(
            Student(
                id = StudentId("student-1"),
                sectionId = sectionId,
                code = StudentCode("12345678901231"),
                fullName = "A",
            ),
            Student(
                id = StudentId("student-2"),
                sectionId = sectionId,
                code = StudentCode("12345678901232"),
                fullName = "B",
            ),
        ),
    )

    @Test
    fun `activities of the current period are listed newest first`() = runTest {
        workedCompetencyRepository.setWorked(sectionId, periodId, Competency.idOf(Area.PPSS, 1), true)
        activityRepository.save(activityOf("activity-1", LocalDate.of(2026, 6, 1)))
        activityRepository.save(activityOf("activity-2", LocalDate.of(2026, 6, 10)))

        val state: ActivitiesUiState = viewModel().state.value

        assertThat(state.activities.map { it.id }).containsExactly(
            ActivityId("activity-2"),
            ActivityId("activity-1"),
        ).inOrder()
    }

    @Test
    fun `the evidence count reflects recorded students out of active ones`() = runTest {
        workedCompetencyRepository.setWorked(sectionId, periodId, Competency.idOf(Area.PPSS, 1), true)
        activityRepository.save(activityOf("activity-1", LocalDate.of(2026, 6, 1)))
        evidenceLevelRepository.save(
            EvidenceLevel(
                EvidenceLevelKey(ActivityId("activity-1"), StudentId("student-1"), Competency.idOf(Area.PPSS, 1)),
                AchievementLevel.A,
            ),
        )

        val row: ActivityRow = viewModel().state.value.activities.single()

        assertThat(row.evidenceRecordedCount).isEqualTo(1)
        assertThat(row.studentCount).isEqualTo(2)
    }

    @Test
    fun `selecting a period switches the activities shown`() = runTest {
        val otherPeriod = Period(
            PeriodId("period-1"),
            schoolYearId,
            1,
            LocalDate.of(2026, 3, 2),
            LocalDate.of(2026, 5, 10),
        )
        workedCompetencyRepository.setWorked(sectionId, PeriodId("period-1"), Competency.idOf(Area.PPSS, 1), true)
        activityRepository.save(
            Activity(
                id = ActivityId("activity-old"),
                sectionId = sectionId,
                periodId = PeriodId("period-1"),
                name = "Old",
                date = LocalDate.of(2026, 4, 1),
                competencyIds = setOf(Competency.idOf(Area.PPSS, 1)),
            ),
        )

        val viewModel: ActivitiesViewModel = viewModel(extraPeriods = listOf(otherPeriod))
        viewModel.onIntent(ActivitiesUiIntent.PeriodSelected(PeriodId("period-1")))

        assertThat(viewModel.state.value.activities.map { it.id }).containsExactly(ActivityId("activity-old"))
    }

    private fun activityOf(id: String, date: LocalDate): Activity = Activity(
        id = ActivityId(id),
        sectionId = sectionId,
        periodId = periodId,
        name = "Actividad $id",
        date = date,
        competencyIds = setOf(Competency.idOf(Area.PPSS, 1)),
    )

    private fun viewModel(extraPeriods: List<Period> = emptyList()): ActivitiesViewModel = ActivitiesViewModel(
        sectionId = sectionId,
        getSection = GetSectionUseCase(FakeSectionRepository(listOf(section))),
        getSchoolYear = GetSchoolYearUseCase(FakeSchoolYearRepository(listOf(schoolYear))),
        getPeriods = GetPeriodsUseCase(FakePeriodRepository(listOf(period) + extraPeriods)),
        getCurrentPeriod = GetCurrentPeriodUseCase(FakePeriodRepository(listOf(period) + extraPeriods), clock),
        getActivities = GetActivitiesUseCase(activityRepository),
        getWorkedCompetencies = GetWorkedCompetenciesUseCase(
            competencyRepository,
            workedCompetencyRepository,
            FakeSectionAreaRepository(),
        ),
        getEvidenceStudentCounts = GetActivityEvidenceStudentCountsUseCase(evidenceLevelRepository),
        getStudents = GetStudentsUseCase(studentRepository),
    )
}
