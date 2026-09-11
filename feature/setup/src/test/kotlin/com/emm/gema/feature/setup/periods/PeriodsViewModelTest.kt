package com.emm.gema.feature.setup.periods

import app.cash.turbine.test
import com.emm.gema.core.domain.schoolyear.GetCurrentPeriodUseCase
import com.emm.gema.core.domain.schoolyear.GetPeriodsUseCase
import com.emm.gema.core.domain.schoolyear.GetSchoolYearUseCase
import com.emm.gema.core.domain.schoolyear.Period
import com.emm.gema.core.domain.schoolyear.PeriodKind
import com.emm.gema.core.domain.schoolyear.PeriodRepository
import com.emm.gema.core.domain.schoolyear.SchoolYear
import com.emm.gema.core.domain.schoolyear.SchoolYearRepository
import com.emm.gema.core.domain.schoolyear.UpdatePeriodsUseCase
import com.emm.gema.core.domain.schoolyear.divide
import com.emm.gema.feature.setup.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

class PeriodsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val schoolYear = SchoolYear(
        id = "2026",
        label = "2026",
        startDate = LocalDate.of(2026, 1, 1),
        endDate = LocalDate.of(2026, 12, 31),
        periodKind = PeriodKind.BIMESTER,
    )
    private val storedPeriods: List<Period> = PeriodKind.BIMESTER
        .divide(schoolYear.startDate, schoolYear.endDate)
        .map { dates ->
            Period("period-${dates.number}", schoolYear.id, dates.number, dates.startDate, dates.endDate)
        }
    private val periodRepository = FakePeriodRepository(storedPeriods)
    private val schoolYearRepository = FakeSchoolYearRepository(schoolYear)

    private fun viewModelAt(today: LocalDate): PeriodsViewModel = PeriodsViewModel(
        schoolYearId = schoolYear.id,
        getSchoolYear = GetSchoolYearUseCase(schoolYearRepository),
        getPeriods = GetPeriodsUseCase(periodRepository),
        getCurrentPeriod = GetCurrentPeriodUseCase(
            repository = periodRepository,
            clock = Clock.fixed(today.atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneId.of("UTC")),
        ),
        updatePeriods = UpdatePeriodsUseCase(periodRepository, schoolYearRepository),
    )

    @Test
    fun `the periods of the school year are listed and the current one is marked`() = runTest {
        val viewModel: PeriodsViewModel = viewModelAt(storedPeriods[1].startDate)

        val state: PeriodsUiState = viewModel.state.value
        assertThat(state.isLoading).isFalse()
        assertThat(state.schoolYearLabel).isEqualTo("2026")
        assertThat(state.periods.map { it.id }).containsExactlyElementsIn(storedPeriods.map { it.id }).inOrder()
        assertThat(state.periods.single { it.isCurrent }.id).isEqualTo("period-2")
    }

    @Test
    fun `no period is marked when today falls outside the school year`() = runTest {
        val viewModel: PeriodsViewModel = viewModelAt(LocalDate.of(2027, 6, 1))

        assertThat(viewModel.state.value.periods.none { it.isCurrent }).isTrue()
    }

    @Test
    fun `an edited date is kept and can be saved`() = runTest {
        val viewModel: PeriodsViewModel = viewModelAt(schoolYear.startDate)
        val newEnd: LocalDate = storedPeriods.first().endDate.minusDays(4)

        viewModel.onIntent(PeriodsUiIntent.EndDateChanged("period-1", newEnd))

        assertThat(viewModel.state.value.periods.first().endDate).isEqualTo(newEnd)
        assertThat(viewModel.state.value.canSave).isTrue()

        viewModel.effects.test {
            viewModel.onIntent(PeriodsUiIntent.SaveClicked)

            assertThat(awaitItem()).isEqualTo(PeriodsUiEffect.NavigateBack)
        }
        assertThat(periodRepository.periods.value.first().endDate).isEqualTo(newEnd)
    }

    @Test
    fun `overlapping periods are reported and block saving`() = runTest {
        val viewModel: PeriodsViewModel = viewModelAt(schoolYear.startDate)

        viewModel.onIntent(PeriodsUiIntent.EndDateChanged("period-1", storedPeriods[1].startDate))

        assertThat(viewModel.state.value.overlapError).isNotNull()
        assertThat(viewModel.state.value.canSave).isFalse()

        viewModel.effects.test {
            viewModel.onIntent(PeriodsUiIntent.SaveClicked)

            expectNoEvents()
        }
    }

    @Test
    fun `a date outside the school year is reported and blocks saving`() = runTest {
        val viewModel: PeriodsViewModel = viewModelAt(schoolYear.startDate)

        viewModel.onIntent(PeriodsUiIntent.StartDateChanged("period-1", schoolYear.startDate.minusDays(2)))

        assertThat(viewModel.state.value.overlapError).isNotNull()
        assertThat(viewModel.state.value.canSave).isFalse()
    }

    @Test
    fun `going back leaves the screen`() = runTest {
        val viewModel: PeriodsViewModel = viewModelAt(schoolYear.startDate)

        viewModel.effects.test {
            viewModel.onIntent(PeriodsUiIntent.BackClicked)

            assertThat(awaitItem()).isEqualTo(PeriodsUiEffect.NavigateBack)
        }
    }

    private class FakePeriodRepository(initial: List<Period>) : PeriodRepository {

        val periods: MutableStateFlow<List<Period>> = MutableStateFlow(initial)

        override fun observeBySchoolYear(schoolYearId: String): Flow<List<Period>> = periods
            .map { stored -> stored.filter { it.schoolYearId == schoolYearId }.sortedBy { it.number } }

        override suspend fun findBySchoolYear(schoolYearId: String): List<Period> = periods.value
            .filter { it.schoolYearId == schoolYearId }
            .sortedBy { it.number }

        override suspend fun findById(id: String): Period? = periods.value.find { it.id == id }

        override suspend fun saveAll(periods: List<Period>) {
            val incoming: Set<String> = periods.map { it.id }.toSet()
            this.periods.value = (this.periods.value.filterNot { it.id in incoming } + periods)
                .sortedBy { it.number }
        }
    }

    private class FakeSchoolYearRepository(private val schoolYear: SchoolYear) : SchoolYearRepository {

        override fun observeAll(): Flow<List<SchoolYear>> = MutableStateFlow(listOf(schoolYear))

        override suspend fun findById(id: String): SchoolYear? = schoolYear.takeIf { it.id == id }

        override suspend fun save(schoolYear: SchoolYear) = Unit
    }
}
