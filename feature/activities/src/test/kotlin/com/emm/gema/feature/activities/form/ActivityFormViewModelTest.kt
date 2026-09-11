package com.emm.gema.feature.activities.form

import app.cash.turbine.test
import com.emm.gema.core.domain.activity.Activity
import com.emm.gema.core.domain.activity.DeleteActivityUseCase
import com.emm.gema.core.domain.activity.EvidenceLevel
import com.emm.gema.core.domain.activity.EvidenceLevelKey
import com.emm.gema.core.domain.activity.GetActivityUseCase
import com.emm.gema.core.domain.activity.SaveActivityUseCase
import com.emm.gema.core.domain.curriculum.Competency
import com.emm.gema.core.domain.curriculum.GetWorkedCompetenciesUseCase
import com.emm.gema.core.domain.evaluation.AchievementLevel
import com.emm.gema.core.domain.id.IdGenerator
import com.emm.gema.core.domain.schoolyear.FindPeriodForDateUseCase
import com.emm.gema.core.domain.schoolyear.GetSchoolYearUseCase
import com.emm.gema.core.domain.schoolyear.Period
import com.emm.gema.core.domain.schoolyear.PeriodKind
import com.emm.gema.core.domain.schoolyear.SchoolYear
import com.emm.gema.core.domain.section.Area
import com.emm.gema.core.domain.section.GetSectionUseCase
import com.emm.gema.core.domain.section.Grade
import com.emm.gema.core.domain.section.Section
import com.emm.gema.feature.activities.FakeActivityRepository
import com.emm.gema.feature.activities.FakeCompetencyRepository
import com.emm.gema.feature.activities.FakeEvidenceLevelRepository
import com.emm.gema.feature.activities.FakePeriodRepository
import com.emm.gema.feature.activities.FakeSchoolYearRepository
import com.emm.gema.feature.activities.FakeSectionAreaRepository
import com.emm.gema.feature.activities.FakeSectionRepository
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
private const val FIRST_PERIOD_ID: String = "period-1"
private const val SECOND_PERIOD_ID: String = "period-2"

class ActivityFormViewModelTest {

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
    private val firstPeriod: Period =
        Period(FIRST_PERIOD_ID, SCHOOL_YEAR_ID, 1, LocalDate.of(2026, 3, 2), LocalDate.of(2026, 5, 10))
    private val secondPeriod: Period =
        Period(SECOND_PERIOD_ID, SCHOOL_YEAR_ID, 2, LocalDate.of(2026, 5, 11), LocalDate.of(2026, 7, 24))
    private val today: LocalDate = LocalDate.of(2026, 3, 10)
    private val clock: Clock = Clock.fixed(today.atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneOffset.UTC)

    private val activityRepository: FakeActivityRepository = FakeActivityRepository()
    private val evidenceLevelRepository: FakeEvidenceLevelRepository = FakeEvidenceLevelRepository(activityRepository)
    private val workedCompetencyRepository: FakeWorkedCompetencyRepository = FakeWorkedCompetencyRepository()
    private val competencyRepository: FakeCompetencyRepository = FakeCompetencyRepository()

    @Test
    fun `the default date resolves to the period it falls into`() = runTest {
        workedCompetencyRepository.setWorked(SECTION_ID, FIRST_PERIOD_ID, Competency.idOf(Area.PPSS, 1), true)

        val state: ActivityFormUiState = viewModel(activityId = null).state.value

        assertThat(state.resolvedPeriodLabel).isEqualTo("I Bimestre")
        assertThat(state.competencyGroups.flatMap { it.competencies }.map { it.id })
            .containsExactly(Competency.idOf(Area.PPSS, 1))
    }

    @Test
    fun `saving derives the period from the date`() = runTest {
        workedCompetencyRepository.setWorked(SECTION_ID, FIRST_PERIOD_ID, Competency.idOf(Area.PPSS, 1), true)
        val viewModel: ActivityFormViewModel = viewModel(activityId = null)
        viewModel.onIntent(ActivityFormUiIntent.NameChanged("Debate del aula"))
        viewModel.onIntent(ActivityFormUiIntent.CompetencyToggled(Competency.idOf(Area.PPSS, 1), true))

        viewModel.effects.test {
            viewModel.onIntent(ActivityFormUiIntent.SaveClicked)

            val effect = awaitItem()
            assertThat(effect).isInstanceOf(ActivityFormUiEffect.NavigateToActivityEvidence::class.java)
        }

        val saved: Activity = activityRepository.activities.value.single()
        assertThat(saved.periodId).isEqualTo(FIRST_PERIOD_ID)
    }

    @Test
    fun `changing the date to another period warns and moves the activity`() = runTest {
        workedCompetencyRepository.setWorked(SECTION_ID, FIRST_PERIOD_ID, Competency.idOf(Area.PPSS, 1), true)
        workedCompetencyRepository.setWorked(SECTION_ID, SECOND_PERIOD_ID, Competency.idOf(Area.PPSS, 1), true)
        val existing = Activity(
            id = "activity-1",
            sectionId = SECTION_ID,
            periodId = FIRST_PERIOD_ID,
            name = "Debate del aula",
            date = LocalDate.of(2026, 3, 10),
            competencyIds = setOf(Competency.idOf(Area.PPSS, 1)),
        )
        activityRepository.save(existing)

        val viewModel: ActivityFormViewModel = viewModel(activityId = "activity-1")
        viewModel.onIntent(ActivityFormUiIntent.DateChanged(LocalDate.of(2026, 6, 1)))

        val state: ActivityFormUiState = viewModel.state.value
        assertThat(state.resolvedPeriodLabel).isEqualTo("II Bimestre")
        assertThat(state.hasPeriodChangeWarning).isTrue()
    }

    @Test
    fun `a date that stops falling into any period is reported, not thrown`() = runTest {
        workedCompetencyRepository.setWorked(SECTION_ID, FIRST_PERIOD_ID, Competency.idOf(Area.PPSS, 1), true)
        val periods: MutableList<Period> = mutableListOf(firstPeriod, secondPeriod)
        val viewModel = ActivityFormViewModel(
            sectionId = SECTION_ID,
            activityId = null,
            clock = clock,
            getSection = GetSectionUseCase(FakeSectionRepository(listOf(section))),
            getSchoolYear = GetSchoolYearUseCase(FakeSchoolYearRepository(listOf(schoolYear))),
            findPeriodForDate = FindPeriodForDateUseCase(FakePeriodRepository(periods)),
            getWorkedCompetencies = GetWorkedCompetenciesUseCase(
                competencyRepository,
                workedCompetencyRepository,
                FakeSectionAreaRepository(),
            ),
            getActivity = GetActivityUseCase(activityRepository),
            saveActivity = SaveActivityUseCase(
                FakeSectionRepository(listOf(section)),
                FindPeriodForDateUseCase(FakePeriodRepository(periods)),
                activityRepository,
                IdGenerator { "activity-new" },
            ),
            deleteActivity = DeleteActivityUseCase(activityRepository, evidenceLevelRepository),
        )
        viewModel.onIntent(ActivityFormUiIntent.NameChanged("Debate del aula"))
        viewModel.onIntent(ActivityFormUiIntent.CompetencyToggled(Competency.idOf(Area.PPSS, 1), true))
        assertThat(viewModel.state.value.canSave).isTrue()

        periods.clear()
        viewModel.onIntent(ActivityFormUiIntent.SaveClicked)

        assertThat(viewModel.state.value.dateError).isEqualTo(ActivityFormMessage.OUTSIDE_PERIODS)
        assertThat(activityRepository.activities.value).isEmpty()
    }

    @Test
    fun `deleting an activity deletes its evidence levels too`() = runTest {
        workedCompetencyRepository.setWorked(SECTION_ID, FIRST_PERIOD_ID, Competency.idOf(Area.PPSS, 1), true)
        val existing = Activity(
            id = "activity-1",
            sectionId = SECTION_ID,
            periodId = FIRST_PERIOD_ID,
            name = "Debate del aula",
            date = LocalDate.of(2026, 3, 10),
            competencyIds = setOf(Competency.idOf(Area.PPSS, 1)),
        )
        activityRepository.save(existing)
        evidenceLevelRepository.save(
            EvidenceLevel(
                EvidenceLevelKey("activity-1", "student-1", Competency.idOf(Area.PPSS, 1)),
                AchievementLevel.A,
            ),
        )
        val viewModel: ActivityFormViewModel = viewModel(activityId = "activity-1")

        viewModel.effects.test {
            viewModel.onIntent(ActivityFormUiIntent.DeleteConfirmed)

            assertThat(awaitItem()).isEqualTo(ActivityFormUiEffect.NavigateBack)
        }

        assertThat(activityRepository.activities.value).isEmpty()
        assertThat(evidenceLevelRepository.levels.value).isEmpty()
    }

    private fun viewModel(activityId: String?): ActivityFormViewModel = ActivityFormViewModel(
        sectionId = SECTION_ID,
        activityId = activityId,
        clock = clock,
        getSection = GetSectionUseCase(FakeSectionRepository(listOf(section))),
        getSchoolYear = GetSchoolYearUseCase(FakeSchoolYearRepository(listOf(schoolYear))),
        findPeriodForDate = FindPeriodForDateUseCase(FakePeriodRepository(listOf(firstPeriod, secondPeriod))),
        getWorkedCompetencies = GetWorkedCompetenciesUseCase(
            competencyRepository,
            workedCompetencyRepository,
            FakeSectionAreaRepository(),
        ),
        getActivity = GetActivityUseCase(activityRepository),
        saveActivity = SaveActivityUseCase(
            FakeSectionRepository(listOf(section)),
            FindPeriodForDateUseCase(FakePeriodRepository(listOf(firstPeriod, secondPeriod))),
            activityRepository,
            IdGenerator { "activity-new" },
        ),
        deleteActivity = DeleteActivityUseCase(activityRepository, evidenceLevelRepository),
    )
}
