package com.emm.gema.feature.evaluation.worked

import app.cash.turbine.test
import com.emm.gema.core.domain.curriculum.Competency
import com.emm.gema.core.domain.curriculum.GetPeriodCompetenciesUseCase
import com.emm.gema.core.domain.curriculum.SetCompetencyWorkedUseCase
import com.emm.gema.core.domain.schoolyear.GetPeriodUseCase
import com.emm.gema.core.domain.schoolyear.GetSchoolYearUseCase
import com.emm.gema.core.domain.schoolyear.Period
import com.emm.gema.core.domain.schoolyear.PeriodKind
import com.emm.gema.core.domain.schoolyear.SchoolYear
import com.emm.gema.core.domain.section.Area
import com.emm.gema.core.domain.section.GetSectionUseCase
import com.emm.gema.core.domain.section.Grade
import com.emm.gema.core.domain.section.Section
import com.emm.gema.feature.evaluation.FakeCompetencyRepository
import com.emm.gema.feature.evaluation.FakePeriodRepository
import com.emm.gema.feature.evaluation.FakeSchoolYearRepository
import com.emm.gema.feature.evaluation.FakeSectionRepository
import com.emm.gema.feature.evaluation.FakeWorkedCompetencyRepository
import com.emm.gema.feature.evaluation.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

private const val SECTION_ID: String = "section-1"
private const val PERIOD_ID: String = "period-2"
private const val SCHOOL_YEAR_ID: String = "year-1"

class WorkedCompetenciesViewModelTest {

    @get:Rule
    val mainDispatcherRule: MainDispatcherRule = MainDispatcherRule()

    private val schoolYear: SchoolYear = SchoolYear(
        id = SCHOOL_YEAR_ID,
        label = "2026",
        startDate = LocalDate.of(2026, 3, 2),
        endDate = LocalDate.of(2026, 12, 18),
        periodKind = PeriodKind.BIMESTER,
    )
    private val section: Section = Section(
        id = SECTION_ID,
        schoolYearId = SCHOOL_YEAR_ID,
        grade = Grade.THIRD,
        name = "A",
    )
    private val period: Period = Period(
        id = PERIOD_ID,
        schoolYearId = SCHOOL_YEAR_ID,
        number = 2,
        startDate = LocalDate.of(2026, 5, 11),
        endDate = LocalDate.of(2026, 7, 24),
    )
    private val workedCompetencyRepository: FakeWorkedCompetencyRepository = FakeWorkedCompetencyRepository()

    @Test
    fun `the screen lists every competency of the area in siagie order`() = runTest {
        val state: WorkedCompetenciesUiState = viewModel().state.value

        assertThat(state.isLoading).isFalse()
        assertThat(state.areaName).isEqualTo("Personal Social")
        assertThat(state.periodLabel).isEqualTo("II Bimestre")
        assertThat(state.competencies.map { it.siagieOrdinal }).containsExactly(1, 2, 3, 4, 5).inOrder()
        assertThat(state.competencies.first().name).isEqualTo("Construye su identidad")
    }

    @Test
    fun `nothing is worked before the teacher marks anything`() = runTest {
        val state: WorkedCompetenciesUiState = viewModel().state.value

        assertThat(state.competencies.none { it.isWorked }).isTrue()
        assertThat(state.selectedCount).isEqualTo(0)
    }

    @Test
    fun `marking a competency persists it and counts it`() = runTest {
        val viewModel: WorkedCompetenciesViewModel = viewModel()
        val target: String = Competency.idOf(Area.PPSS, 2)

        viewModel.onIntent(WorkedCompetenciesUiIntent.CompetencyToggled(target, isWorked = true))

        val state: WorkedCompetenciesUiState = viewModel.state.value
        assertThat(state.competencies.single { it.id == target }.isWorked).isTrue()
        assertThat(state.selectedCount).isEqualTo(1)
        assertThat(workedCompetencyRepository.rows.value).containsExactly(Triple(SECTION_ID, PERIOD_ID, target))
    }

    @Test
    fun `unmarking a competency drops it again`() = runTest {
        val viewModel: WorkedCompetenciesViewModel = viewModel()
        val target: String = Competency.idOf(Area.PPSS, 2)

        viewModel.onIntent(WorkedCompetenciesUiIntent.CompetencyToggled(target, isWorked = true))
        viewModel.onIntent(WorkedCompetenciesUiIntent.CompetencyToggled(target, isWorked = false))

        assertThat(viewModel.state.value.selectedCount).isEqualTo(0)
        assertThat(workedCompetencyRepository.rows.value).isEmpty()
    }

    @Test
    fun `a failed write tells the teacher`() = runTest {
        val viewModel: WorkedCompetenciesViewModel = viewModel()
        workedCompetencyRepository.failsOnce = true

        viewModel.effects.test {
            viewModel.onIntent(
                WorkedCompetenciesUiIntent.CompetencyToggled(Competency.idOf(Area.PPSS, 1), isWorked = true),
            )

            assertThat(awaitItem()).isInstanceOf(WorkedCompetenciesUiEffect.ShowMessage::class.java)
        }
    }

    @Test
    fun `the back intent leaves the screen`() = runTest {
        val viewModel: WorkedCompetenciesViewModel = viewModel()

        viewModel.effects.test {
            viewModel.onIntent(WorkedCompetenciesUiIntent.BackClicked)

            assertThat(awaitItem()).isEqualTo(WorkedCompetenciesUiEffect.NavigateBack)
        }
    }

    private fun viewModel(area: Area = Area.PPSS): WorkedCompetenciesViewModel = WorkedCompetenciesViewModel(
        sectionId = SECTION_ID,
        periodId = PERIOD_ID,
        area = area,
        getSection = GetSectionUseCase(FakeSectionRepository(listOf(section))),
        getPeriod = GetPeriodUseCase(FakePeriodRepository(listOf(period))),
        getSchoolYear = GetSchoolYearUseCase(FakeSchoolYearRepository(listOf(schoolYear))),
        getPeriodCompetencies = GetPeriodCompetenciesUseCase(
            competencyRepository = FakeCompetencyRepository(),
            workedCompetencyRepository = workedCompetencyRepository,
        ),
        setCompetencyWorked = SetCompetencyWorkedUseCase(workedCompetencyRepository),
    )
}
