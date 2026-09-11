package com.emm.gema.feature.setup.year

import app.cash.turbine.test
import com.emm.gema.core.domain.schoolyear.PeriodKind
import com.emm.gema.feature.setup.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class SetupYearViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val clock: Clock = Clock.fixed(Instant.parse("2026-04-15T08:00:00Z"), ZoneOffset.UTC)

    private val viewModel = SetupYearViewModel(clock)

    @Test
    fun `the form opens prefilled from the device date`() {
        val state: SetupYearUiState = viewModel.state.value

        assertThat(state.yearLabel).isEqualTo("2026")
        assertThat(state.startDate).isEqualTo(LocalDate.of(2026, 3, 1))
        assertThat(state.endDate).isEqualTo(LocalDate.of(2026, 12, 20))
        assertThat(state.canContinue).isTrue()
    }

    @Test
    fun `the prefilled year is divided into one row per bimester`() {
        val state: SetupYearUiState = viewModel.state.value

        assertThat(state.periods).hasSize(PeriodKind.BIMESTER.periodCount)
        assertThat(state.periods.map { it.ordinal }).containsExactly(1, 2, 3, 4).inOrder()
        assertThat(state.periods.first().startDate).isEqualTo(LocalDate.of(2026, 3, 1))
        assertThat(state.periods.last().endDate).isEqualTo(LocalDate.of(2026, 12, 20))
    }

    @Test
    fun `switching to trimester redivides the year into three rows`() {
        viewModel.onIntent(SetupYearUiIntent.PeriodKindSelected(PeriodKind.TRIMESTER))

        val state: SetupYearUiState = viewModel.state.value
        assertThat(state.periods).hasSize(PeriodKind.TRIMESTER.periodCount)
        assertThat(state.periods.last().endDate).isEqualTo(LocalDate.of(2026, 12, 20))
    }

    @Test
    fun `changing the school year dates redivides the periods`() {
        viewModel.onIntent(SetupYearUiIntent.EndDateChanged(LocalDate.of(2026, 12, 18)))

        assertThat(viewModel.state.value.periods.last().endDate).isEqualTo(LocalDate.of(2026, 12, 18))
    }

    @Test
    fun `a year that ends before it starts is reported and blocks continuing`() {
        viewModel.onIntent(SetupYearUiIntent.StartDateChanged(LocalDate.of(2026, 12, 18)))
        viewModel.onIntent(SetupYearUiIntent.EndDateChanged(LocalDate.of(2026, 3, 2)))

        val state: SetupYearUiState = viewModel.state.value
        assertThat(state.dateRangeError).isNotNull()
        assertThat(state.periods).isEmpty()
        assertThat(state.canContinue).isFalse()
    }

    @Test
    fun `an empty year label is reported and blocks continuing`() {
        viewModel.onIntent(SetupYearUiIntent.YearLabelChanged("  "))

        assertThat(viewModel.state.value.yearLabelError).isNotNull()
        assertThat(viewModel.state.value.canContinue).isFalse()
    }

    @Test
    fun `tapping a period row opens its editor`() {
        viewModel.onIntent(SetupYearUiIntent.PeriodClicked(2))

        val editor: PeriodEditorState? = viewModel.state.value.editor
        assertThat(editor).isNotNull()
        assertThat(editor?.ordinal).isEqualTo(2)
        assertThat(editor?.startDate).isEqualTo(viewModel.state.value.periods[1].startDate)
    }

    @Test
    fun `dismissing the editor keeps the period untouched`() {
        val original: LocalDate = viewModel.state.value.periods.first().endDate
        viewModel.onIntent(SetupYearUiIntent.PeriodClicked(1))
        viewModel.onIntent(SetupYearUiIntent.EditorEndDateChanged(LocalDate.of(2026, 5, 20)))

        viewModel.onIntent(SetupYearUiIntent.EditorDismissed)

        assertThat(viewModel.state.value.editor).isNull()
        assertThat(viewModel.state.value.periods.first().endDate).isEqualTo(original)
    }

    @Test
    fun `confirming the editor writes the new dates into its period`() {
        viewModel.onIntent(SetupYearUiIntent.PeriodClicked(1))
        viewModel.onIntent(SetupYearUiIntent.EditorEndDateChanged(LocalDate.of(2026, 5, 20)))

        viewModel.onIntent(SetupYearUiIntent.EditorConfirmed)

        assertThat(viewModel.state.value.editor).isNull()
        assertThat(viewModel.state.value.periods.first().endDate).isEqualTo(LocalDate.of(2026, 5, 20))
    }

    @Test
    fun `the editor reports dates that overlap another period`() {
        viewModel.onIntent(SetupYearUiIntent.PeriodClicked(1))

        viewModel.onIntent(SetupYearUiIntent.EditorEndDateChanged(LocalDate.of(2026, 8, 1)))

        assertThat(viewModel.state.value.editor?.error).isNotNull()
    }

    @Test
    fun `overlapping periods are reported on the offending row and block continuing`() {
        viewModel.onIntent(SetupYearUiIntent.PeriodClicked(1))
        viewModel.onIntent(SetupYearUiIntent.EditorEndDateChanged(LocalDate.of(2026, 8, 1)))

        viewModel.onIntent(SetupYearUiIntent.EditorConfirmed)

        val state: SetupYearUiState = viewModel.state.value
        assertThat(state.periods.first().error).isNotNull()
        assertThat(state.canContinue).isFalse()
    }

    @Test
    fun `the editor reports dates that leave the school year`() {
        viewModel.onIntent(SetupYearUiIntent.PeriodClicked(1))

        viewModel.onIntent(SetupYearUiIntent.EditorStartDateChanged(LocalDate.of(2026, 1, 5)))

        assertThat(viewModel.state.value.editor?.error).isNotNull()
    }

    @Test
    fun `a period that leaves the school year is reported on its row and blocks continuing`() {
        viewModel.onIntent(SetupYearUiIntent.PeriodClicked(1))
        viewModel.onIntent(SetupYearUiIntent.EditorStartDateChanged(LocalDate.of(2026, 1, 5)))

        viewModel.onIntent(SetupYearUiIntent.EditorConfirmed)

        assertThat(viewModel.state.value.periods.first().error).isNotNull()
        assertThat(viewModel.state.value.canContinue).isFalse()
    }

    @Test
    fun `continuing carries the draft to the section step`() = runTest {
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
        viewModel.onIntent(SetupYearUiIntent.YearLabelChanged(""))

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
}
