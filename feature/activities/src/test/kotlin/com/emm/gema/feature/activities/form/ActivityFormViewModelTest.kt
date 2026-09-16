package com.emm.gema.feature.activities.form

import app.cash.turbine.test
import com.emm.gema.core.domain.activity.Activity
import com.emm.gema.core.domain.activity.ActivityId
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
import com.emm.gema.core.domain.student.StudentId
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
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneOffset
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

private val sectionId: SectionId = SectionId("section-1")
private val schoolYearId: SchoolYearId = SchoolYearId("year-1")
private val firstPeriodId: PeriodId = PeriodId("period-1")
private val secondPeriodId: PeriodId = PeriodId("period-2")

class ActivityFormViewModelTest {

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
    private val firstPeriod: Period =
        Period(firstPeriodId, schoolYearId, 1, LocalDate.of(2026, 3, 2), LocalDate.of(2026, 5, 10))
    private val secondPeriod: Period =
        Period(secondPeriodId, schoolYearId, 2, LocalDate.of(2026, 5, 11), LocalDate.of(2026, 7, 24))
    private val today: LocalDate = LocalDate.of(2026, 3, 10)
    private val clock: Clock = Clock.fixed(today.atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneOffset.UTC)
    private val existingActivity: Activity = Activity(
        id = ActivityId("activity-1"),
        sectionId = sectionId,
        periodId = firstPeriodId,
        name = "Debate del aula",
        date = LocalDate.of(2026, 3, 10),
        competencyIds = setOf(Competency.idOf(Area.PPSS, 1)),
    )

    private val activityRepository: FakeActivityRepository = FakeActivityRepository()
    private val evidenceLevelRepository: FakeEvidenceLevelRepository = FakeEvidenceLevelRepository(activityRepository)
    private val workedCompetencyRepository: FakeWorkedCompetencyRepository = FakeWorkedCompetencyRepository()
    private val competencyRepository: FakeCompetencyRepository = FakeCompetencyRepository()

    @Test
    fun `the default date resolves to the period it falls into`() = runTest {
        workedCompetencyRepository.setWorked(sectionId, firstPeriodId, Competency.idOf(Area.PPSS, 1), true)

        val state: ActivityFormUiState = viewModel(activityId = null).state.value

        assertThat(state.resolvedPeriodLabel).isEqualTo("I Bimestre")
        assertThat(state.competencyGroups.flatMap { it.competencies }.map { it.id })
            .containsExactly(Competency.idOf(Area.PPSS, 1))
    }

    @Test
    fun `saving derives the period from the date`() = runTest {
        workedCompetencyRepository.setWorked(sectionId, firstPeriodId, Competency.idOf(Area.PPSS, 1), true)
        val viewModel: ActivityFormViewModel = viewModel(activityId = null)
        viewModel.onIntent(ActivityFormUiIntent.NameChanged("Debate del aula"))
        viewModel.onIntent(ActivityFormUiIntent.CompetencyToggled(Competency.idOf(Area.PPSS, 1), true))

        viewModel.effects.test {
            viewModel.onIntent(ActivityFormUiIntent.SaveClicked)

            val effect = awaitItem()
            assertThat(effect).isInstanceOf(ActivityFormUiEffect.NavigateToActivityEvidence::class.java)
        }

        val saved: Activity = activityRepository.activities.value.single()
        assertThat(saved.periodId).isEqualTo(firstPeriodId)
    }

    @Test
    fun `changing the date to another period sets the origin period label`() = runTest {
        workedCompetencyRepository.setWorked(sectionId, firstPeriodId, Competency.idOf(Area.PPSS, 1), true)
        workedCompetencyRepository.setWorked(sectionId, secondPeriodId, Competency.idOf(Area.PPSS, 1), true)
        activityRepository.save(existingActivity)

        val viewModel: ActivityFormViewModel = viewModel(activityId = ActivityId("activity-1"))
        viewModel.onIntent(ActivityFormUiIntent.DateChanged(LocalDate.of(2026, 6, 1)))

        val state: ActivityFormUiState = viewModel.state.value
        assertThat(state.resolvedPeriodLabel).isEqualTo("II Bimestre")
        assertThat(state.periodChangeFromLabel).isEqualTo("I Bimestre")
    }

    @Test
    fun `changing the date back to the original period clears the origin period label`() = runTest {
        workedCompetencyRepository.setWorked(sectionId, firstPeriodId, Competency.idOf(Area.PPSS, 1), true)
        workedCompetencyRepository.setWorked(sectionId, secondPeriodId, Competency.idOf(Area.PPSS, 1), true)
        activityRepository.save(existingActivity)

        val viewModel: ActivityFormViewModel = viewModel(activityId = ActivityId("activity-1"))
        viewModel.onIntent(ActivityFormUiIntent.DateChanged(LocalDate.of(2026, 6, 1)))
        viewModel.onIntent(ActivityFormUiIntent.DateChanged(LocalDate.of(2026, 3, 10)))

        assertThat(viewModel.state.value.periodChangeFromLabel).isNull()
    }

    @Test
    fun `create mode never sets the origin period label`() = runTest {
        workedCompetencyRepository.setWorked(sectionId, firstPeriodId, Competency.idOf(Area.PPSS, 1), true)
        workedCompetencyRepository.setWorked(sectionId, secondPeriodId, Competency.idOf(Area.PPSS, 1), true)
        val viewModel: ActivityFormViewModel = viewModel(activityId = null)
        viewModel.onIntent(ActivityFormUiIntent.CompetencyToggled(Competency.idOf(Area.PPSS, 1), true))

        viewModel.onIntent(ActivityFormUiIntent.DateChanged(LocalDate.of(2026, 6, 1)))

        assertThat(viewModel.state.value.periodChangeFromLabel).isNull()
    }

    @Test
    fun `the origin period label survives the Period's dates being edited`() = runTest {
        workedCompetencyRepository.setWorked(sectionId, firstPeriodId, Competency.idOf(Area.PPSS, 1), true)
        workedCompetencyRepository.setWorked(sectionId, secondPeriodId, Competency.idOf(Area.PPSS, 1), true)
        activityRepository.save(existingActivity)
        val editedFirstPeriod: Period = Period(
            firstPeriodId,
            schoolYearId,
            1,
            LocalDate.of(2026, 4, 1),
            LocalDate.of(2026, 5, 10),
        )

        val viewModel: ActivityFormViewModel = viewModel(
            activityId = ActivityId("activity-1"),
            periods = listOf(editedFirstPeriod, secondPeriod),
        )
        viewModel.onIntent(ActivityFormUiIntent.DateChanged(LocalDate.of(2026, 6, 1)))

        assertThat(viewModel.state.value.periodChangeFromLabel).isEqualTo("I Bimestre")
    }

    @Test
    fun `a date that stops falling into any period is reported, not thrown`() = runTest {
        workedCompetencyRepository.setWorked(sectionId, firstPeriodId, Competency.idOf(Area.PPSS, 1), true)
        val periods: MutableList<Period> = mutableListOf(firstPeriod, secondPeriod)
        val viewModel = ActivityFormViewModel(
            sectionId = sectionId,
            activityId = null,
            clock = clock,
            getSection = GetSectionUseCase(FakeSectionRepository(listOf(section))),
            getSchoolYear = GetSchoolYearUseCase(FakeSchoolYearRepository(listOf(schoolYear))),
            getPeriods = GetPeriodsUseCase(FakePeriodRepository(periods)),
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
        workedCompetencyRepository.setWorked(sectionId, firstPeriodId, Competency.idOf(Area.PPSS, 1), true)
        activityRepository.save(existingActivity)
        evidenceLevelRepository.save(
            EvidenceLevel(
                EvidenceLevelKey(ActivityId("activity-1"), StudentId("student-1"), Competency.idOf(Area.PPSS, 1)),
                AchievementLevel.A,
            ),
        )
        val viewModel: ActivityFormViewModel = viewModel(activityId = ActivityId("activity-1"))

        viewModel.effects.test {
            viewModel.onIntent(ActivityFormUiIntent.DeleteConfirmed)

            assertThat(awaitItem()).isEqualTo(ActivityFormUiEffect.NavigateBack)
        }

        assertThat(activityRepository.activities.value).isEmpty()
        assertThat(evidenceLevelRepository.levels.value).isEmpty()
    }

    private fun viewModel(
        activityId: ActivityId?,
        periods: List<Period> = listOf(firstPeriod, secondPeriod),
    ): ActivityFormViewModel = ActivityFormViewModel(
        sectionId = sectionId,
        activityId = activityId,
        clock = clock,
        getSection = GetSectionUseCase(FakeSectionRepository(listOf(section))),
        getSchoolYear = GetSchoolYearUseCase(FakeSchoolYearRepository(listOf(schoolYear))),
        getPeriods = GetPeriodsUseCase(FakePeriodRepository(periods)),
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
}
