package com.emm.gema.feature.activities.list

import com.emm.gema.core.domain.activity.Activity
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
import com.emm.gema.core.domain.schoolyear.PeriodKind
import com.emm.gema.core.domain.schoolyear.SchoolYear
import com.emm.gema.core.domain.section.Area
import com.emm.gema.core.domain.section.GetSectionUseCase
import com.emm.gema.core.domain.section.Grade
import com.emm.gema.core.domain.section.Section
import com.emm.gema.core.domain.student.GetStudentsUseCase
import com.emm.gema.core.domain.student.Student
import com.emm.gema.core.domain.student.StudentCode
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
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneOffset

private const val SECTION_ID: String = "section-1"
private const val SCHOOL_YEAR_ID: String = "year-1"
private const val PERIOD_ID: String = "period-2"

class ActivitiesViewModelTest {

    @get:Rule
    val mainDispatcherRule: MainDispatcherRule = MainDispatcherRule()

    private val schoolYear: SchoolYear = SchoolYear(
        id = SCHOOL_YEAR_ID,
        label = "2026",
        startDate = LocalDate.of(2026, 3, 2),
        endDate = LocalDate.of(2026, 12, 18),
        periodKind = PeriodKind.BIMESTER,
    )
    private val section: Section = Section(SECTION_ID, SCHOOL_YEAR_ID, Grade.THIRD, "A")
    private val period: Period =
        Period(PERIOD_ID, SCHOOL_YEAR_ID, 2, LocalDate.of(2026, 5, 11), LocalDate.of(2026, 7, 24))
    private val today: LocalDate = LocalDate.of(2026, 6, 1)
    private val clock: Clock = Clock.fixed(today.atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneOffset.UTC)

    private val activityRepository: FakeActivityRepository = FakeActivityRepository()
    private val evidenceLevelRepository: FakeEvidenceLevelRepository = FakeEvidenceLevelRepository(activityRepository)
    private val workedCompetencyRepository: FakeWorkedCompetencyRepository = FakeWorkedCompetencyRepository()
    private val competencyRepository: FakeCompetencyRepository = FakeCompetencyRepository()
    private val studentRepository: FakeStudentRepository = FakeStudentRepository(
        listOf(
            Student(id = "student-1", sectionId = SECTION_ID, code = StudentCode("12345678901231"), fullName = "A"),
            Student(id = "student-2", sectionId = SECTION_ID, code = StudentCode("12345678901232"), fullName = "B"),
        ),
    )

    @Test
    fun `activities of the current period are listed newest first`() = runTest {
        workedCompetencyRepository.setWorked(SECTION_ID, PERIOD_ID, Competency.idOf(Area.PPSS, 1), true)
        activityRepository.save(activityOf("activity-1", LocalDate.of(2026, 6, 1)))
        activityRepository.save(activityOf("activity-2", LocalDate.of(2026, 6, 10)))

        val state: ActivitiesUiState = viewModel().state.value

        assertThat(state.activities.map { it.id }).containsExactly("activity-2", "activity-1").inOrder()
    }

    @Test
    fun `the evidence count reflects recorded students out of active ones`() = runTest {
        workedCompetencyRepository.setWorked(SECTION_ID, PERIOD_ID, Competency.idOf(Area.PPSS, 1), true)
        activityRepository.save(activityOf("activity-1", LocalDate.of(2026, 6, 1)))
        evidenceLevelRepository.save(
            EvidenceLevel(
                EvidenceLevelKey("activity-1", "student-1", Competency.idOf(Area.PPSS, 1)),
                AchievementLevel.A,
            ),
        )

        val row: ActivityRow = viewModel().state.value.activities.single()

        assertThat(row.evidenceRecordedCount).isEqualTo(1)
        assertThat(row.studentCount).isEqualTo(2)
    }

    @Test
    fun `selecting a period switches the activities shown`() = runTest {
        val otherPeriod = Period("period-1", SCHOOL_YEAR_ID, 1, LocalDate.of(2026, 3, 2), LocalDate.of(2026, 5, 10))
        workedCompetencyRepository.setWorked(SECTION_ID, "period-1", Competency.idOf(Area.PPSS, 1), true)
        activityRepository.save(
            Activity(
                id = "activity-old",
                sectionId = SECTION_ID,
                periodId = "period-1",
                name = "Old",
                date = LocalDate.of(2026, 4, 1),
                competencyIds = setOf(Competency.idOf(Area.PPSS, 1)),
            ),
        )

        val viewModel: ActivitiesViewModel = viewModel(extraPeriods = listOf(otherPeriod))
        viewModel.onIntent(ActivitiesUiIntent.PeriodSelected("period-1"))

        assertThat(viewModel.state.value.activities.map { it.id }).containsExactly("activity-old")
    }

    private fun activityOf(id: String, date: LocalDate): Activity = Activity(
        id = id,
        sectionId = SECTION_ID,
        periodId = PERIOD_ID,
        name = "Actividad $id",
        date = date,
        competencyIds = setOf(Competency.idOf(Area.PPSS, 1)),
    )

    private fun viewModel(extraPeriods: List<Period> = emptyList()): ActivitiesViewModel = ActivitiesViewModel(
        sectionId = SECTION_ID,
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
