package com.emm.gema.feature.setup.year

import app.cash.turbine.test
import com.emm.gema.core.domain.schoolyear.PeriodKind
import com.emm.gema.feature.setup.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class SetupYearViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val viewModel = SetupYearViewModel()

    @Test
    fun `the form starts empty and cannot continue`() {
        val state: SetupYearUiState = viewModel.state.value

        assertThat(state.isLoading).isFalse()
        assertThat(state.yearLabel).isEmpty()
        assertThat(state.periods).isEmpty()
        assertThat(state.canContinue).isFalse()
    }

    @Test
    fun `choosing the dates prefills one period per bimester`() {
        fillValidYear()

        val state: SetupYearUiState = viewModel.state.value
        assertThat(state.periods).hasSize(PeriodKind.BIMESTER.periodCount)
        assertThat(state.periods.map { it.ordinal }).containsExactly(1, 2, 3, 4).inOrder()
        assertThat(state.periods.first().startDate).isEqualTo(LocalDate.of(2026, 3, 2))
        assertThat(state.periods.last().endDate).isEqualTo(LocalDate.of(2026, 12, 18))
        assertThat(state.canContinue).isTrue()
    }

    @Test
    fun `switching to trimester redivides the year`() {
        fillValidYear()

        viewModel.onIntent(SetupYearUiIntent.PeriodKindSelected(PeriodKind.TRIMESTER))

        assertThat(viewModel.state.value.periods).hasSize(PeriodKind.TRIMESTER.periodCount)
        assertThat(viewModel.state.value.periods.last().endDate).isEqualTo(LocalDate.of(2026, 12, 18))
    }

    @Test
    fun `a year that ends before it starts is reported and blocks continuing`() {
        viewModel.onIntent(SetupYearUiIntent.YearLabelChanged("2026"))
        viewModel.onIntent(SetupYearUiIntent.StartDateChanged(LocalDate.of(2026, 12, 18)))
        viewModel.onIntent(SetupYearUiIntent.EndDateChanged(LocalDate.of(2026, 3, 2)))

        val state: SetupYearUiState = viewModel.state.value
        assertThat(state.dateRangeError).isNotNull()
        assertThat(state.periods).isEmpty()
        assertThat(state.canContinue).isFalse()
    }

    @Test
    fun `an empty year label is reported and blocks continuing`() {
        fillValidYear()

        viewModel.onIntent(SetupYearUiIntent.YearLabelChanged("  "))

        assertThat(viewModel.state.value.yearLabelError).isNotNull()
        assertThat(viewModel.state.value.canContinue).isFalse()
    }

    @Test
    fun `an edited period date is kept`() {
        fillValidYear()

        viewModel.onIntent(SetupYearUiIntent.PeriodEndDateChanged(1, LocalDate.of(2026, 5, 20)))

        assertThat(viewModel.state.value.periods.first().endDate).isEqualTo(LocalDate.of(2026, 5, 20))
    }

    @Test
    fun `overlapping periods are reported on the offending row and block continuing`() {
        fillValidYear()

        viewModel.onIntent(SetupYearUiIntent.PeriodEndDateChanged(1, LocalDate.of(2026, 8, 1)))

        val state: SetupYearUiState = viewModel.state.value
        assertThat(state.periods.first().error).isNotNull()
        assertThat(state.canContinue).isFalse()
    }

    @Test
    fun `a period that leaves the school year is reported`() {
        fillValidYear()

        viewModel.onIntent(SetupYearUiIntent.PeriodStartDateChanged(1, LocalDate.of(2026, 1, 5)))

        assertThat(viewModel.state.value.periods.first().error).isNotNull()
        assertThat(viewModel.state.value.canContinue).isFalse()
    }

    @Test
    fun `continuing carries the draft to the section step`() = runTest {
        fillValidYear()

        viewModel.effects.test {
            viewModel.onIntent(SetupYearUiIntent.ContinueClicked)

            val effect = awaitItem()
            assertThat(effect).isInstanceOf(SetupYearUiEffect.NavigateToSetupSection::class.java)
            val draft: SchoolYearDraft = (effect as SetupYearUiEffect.NavigateToSetupSection).draft
            assertThat(draft.label).isEqualTo("2026")
            assertThat(draft.periodKind).isEqualTo(PeriodKind.BIMESTER)
            assertThat(draft.periods).hasSize(PeriodKind.BIMESTER.periodCount)
        }
    }

    @Test
    fun `continuing with an invalid form emits nothing`() = runTest {
        viewModel.effects.test {
            viewModel.onIntent(SetupYearUiIntent.ContinueClicked)

            expectNoEvents()
        }
    }

    @Test
    fun `going back leaves the flow`() = runTest {
        viewModel.effects.test {
            viewModel.onIntent(SetupYearUiIntent.BackClicked)

            assertThat(awaitItem()).isEqualTo(SetupYearUiEffect.NavigateBack)
        }
    }

    private fun fillValidYear() {
        viewModel.onIntent(SetupYearUiIntent.YearLabelChanged("2026"))
        viewModel.onIntent(SetupYearUiIntent.StartDateChanged(LocalDate.of(2026, 3, 2)))
        viewModel.onIntent(SetupYearUiIntent.EndDateChanged(LocalDate.of(2026, 12, 18)))
    }
}
