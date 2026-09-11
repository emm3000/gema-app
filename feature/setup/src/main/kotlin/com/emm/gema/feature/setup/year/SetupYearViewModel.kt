package com.emm.gema.feature.setup.year

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.gema.core.domain.schoolyear.PeriodDates
import com.emm.gema.core.domain.schoolyear.PeriodKind
import com.emm.gema.core.domain.schoolyear.divide
import com.emm.gema.feature.setup.errorWithin
import com.emm.gema.feature.setup.labelFor
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit

private const val MISSING_LABEL_ERROR: String = "Escribe un nombre para el año escolar"
private const val INVALID_RANGE_ERROR: String = "El año escolar termina después de empezar"
private const val TOO_SHORT_ERROR: String = "El año escolar es muy corto para sus periodos"

class SetupYearViewModel : ViewModel() {

    private val _state: MutableStateFlow<SetupYearUiState> = MutableStateFlow(SetupYearUiState())
    val state: StateFlow<SetupYearUiState> = _state.asStateFlow()

    private val _effects: Channel<SetupYearUiEffect> = Channel(Channel.BUFFERED)
    val effects: Flow<SetupYearUiEffect> = _effects.receiveAsFlow()

    fun onIntent(intent: SetupYearUiIntent) {
        when (intent) {
            is SetupYearUiIntent.YearLabelChanged -> update { it.copy(yearLabel = intent.value) }
            is SetupYearUiIntent.StartDateChanged -> redivide { it.copy(startDate = intent.value) }
            is SetupYearUiIntent.EndDateChanged -> redivide { it.copy(endDate = intent.value) }
            is SetupYearUiIntent.PeriodKindSelected -> redivide { it.copy(periodKind = intent.kind) }
            is SetupYearUiIntent.PeriodStartDateChanged -> editPeriod(intent.ordinal) {
                it.copy(startDate = intent.value)
            }
            is SetupYearUiIntent.PeriodEndDateChanged -> editPeriod(intent.ordinal) {
                it.copy(endDate = intent.value)
            }
            SetupYearUiIntent.ContinueClicked -> continueToSection()
            SetupYearUiIntent.BackClicked -> emit(SetupYearUiEffect.NavigateBack)
        }
    }

    private fun continueToSection() {
        val current: SetupYearUiState = _state.value
        val startDate: LocalDate = current.startDate ?: return
        val endDate: LocalDate = current.endDate ?: return
        if (!current.canContinue) return

        emit(
            SetupYearUiEffect.NavigateToSetupSection(
                SchoolYearDraft(
                    label = current.yearLabel.trim(),
                    startDate = startDate,
                    endDate = endDate,
                    periodKind = current.periodKind,
                    periods = current.periods.map { PeriodDates(it.ordinal, it.startDate, it.endDate) },
                )
            )
        )
    }

    private fun editPeriod(ordinal: Int, edit: (PeriodDraftRow) -> PeriodDraftRow) = update { current ->
        current.copy(
            periods = current.periods.map { row -> if (row.ordinal == ordinal) edit(row) else row },
        )
    }

    private fun redivide(change: (SetupYearUiState) -> SetupYearUiState) = update { current ->
        val changed: SetupYearUiState = change(current)
        changed.copy(periods = dividePeriods(changed))
    }

    private fun update(change: (SetupYearUiState) -> SetupYearUiState) {
        _state.value = validate(change(_state.value))
    }

    private fun emit(effect: SetupYearUiEffect) {
        viewModelScope.launch { _effects.send(effect) }
    }

    private fun dividePeriods(state: SetupYearUiState): List<PeriodDraftRow> {
        val startDate: LocalDate = state.startDate ?: return emptyList()
        val endDate: LocalDate = state.endDate ?: return emptyList()
        if (!isLongEnough(startDate, endDate, state.periodKind)) return emptyList()

        return state.periodKind.divide(startDate, endDate).map { dates ->
            PeriodDraftRow(
                ordinal = dates.number,
                label = state.periodKind.labelFor(dates.number),
                startDate = dates.startDate,
                endDate = dates.endDate,
                error = null,
            )
        }
    }

    private fun validate(state: SetupYearUiState): SetupYearUiState {
        val ranges: List<PeriodDates> = state.periods.map { PeriodDates(it.ordinal, it.startDate, it.endDate) }
        val periods: List<PeriodDraftRow> = state.periods.mapIndexed { index, row ->
            row.copy(error = errorFor(ranges[index], ranges, state))
        }
        val dateRangeError: String? = dateRangeErrorFor(state)
        val yearLabelError: String? = MISSING_LABEL_ERROR.takeIf { state.yearLabel.isBlank() }

        return state.copy(
            periods = periods,
            yearLabelError = yearLabelError,
            dateRangeError = dateRangeError,
            canContinue = yearLabelError == null &&
                dateRangeError == null &&
                periods.isNotEmpty() &&
                periods.none { it.error != null },
        )
    }

    private fun dateRangeErrorFor(state: SetupYearUiState): String? {
        val startDate: LocalDate = state.startDate ?: return null
        val endDate: LocalDate = state.endDate ?: return null
        if (!endDate.isAfter(startDate)) return INVALID_RANGE_ERROR
        if (!isLongEnough(startDate, endDate, state.periodKind)) return TOO_SHORT_ERROR
        return null
    }

    private fun errorFor(period: PeriodDates, periods: List<PeriodDates>, state: SetupYearUiState): String? {
        val startDate: LocalDate = state.startDate ?: return null
        val endDate: LocalDate = state.endDate ?: return null
        return period.errorWithin(periods, startDate, endDate)
    }

    private fun isLongEnough(startDate: LocalDate, endDate: LocalDate, periodKind: PeriodKind): Boolean =
        ChronoUnit.DAYS.between(startDate, endDate) + 1 >= periodKind.periodCount
}
