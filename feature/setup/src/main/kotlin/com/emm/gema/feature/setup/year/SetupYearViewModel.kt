package com.emm.gema.feature.setup.year

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.gema.core.domain.schoolyear.PeriodDates
import com.emm.gema.core.domain.schoolyear.PeriodKind
import com.emm.gema.core.domain.schoolyear.divide
import com.emm.gema.feature.setup.PeriodRangeError
import com.emm.gema.feature.setup.errorWithin
import com.emm.gema.core.domain.schoolyear.labelFor
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class SetupYearViewModel(clock: Clock) : ViewModel() {

    private val _state: MutableStateFlow<SetupYearUiState> = MutableStateFlow(prefilledState(clock))
    val state: StateFlow<SetupYearUiState> = _state.asStateFlow()

    private val _effects: Channel<SetupYearUiEffect> = Channel(Channel.BUFFERED)
    val effects: Flow<SetupYearUiEffect> = _effects.receiveAsFlow()

    fun onIntent(intent: SetupYearUiIntent) {
        when (intent) {
            is SetupYearUiIntent.YearLabelChanged -> update { it.copy(yearLabel = intent.value) }
            is SetupYearUiIntent.StartDateChanged -> redivide { it.copy(startDate = intent.value) }
            is SetupYearUiIntent.EndDateChanged -> redivide { it.copy(endDate = intent.value) }
            is SetupYearUiIntent.PeriodKindSelected -> redivide { it.copy(periodKind = intent.kind) }
            is SetupYearUiIntent.PeriodClicked -> openEditor(intent.ordinal)
            is SetupYearUiIntent.EditorStartDateChanged -> editEditor { it.copy(startDate = intent.value) }
            is SetupYearUiIntent.EditorEndDateChanged -> editEditor { it.copy(endDate = intent.value) }
            SetupYearUiIntent.EditorConfirmed -> confirmEditor()
            SetupYearUiIntent.EditorDismissed -> update { it.copy(editor = null) }
            SetupYearUiIntent.ContinueClicked -> continueToSection()
            SetupYearUiIntent.BackClicked -> emit(SetupYearUiEffect.NavigateBack)
        }
    }

    private fun openEditor(ordinal: Int) = update { current ->
        val row: PeriodDraftRow = current.periods.firstOrNull { it.ordinal == ordinal } ?: return@update current
        current.copy(
            editor = PeriodEditorState(
                ordinal = row.ordinal,
                label = row.label,
                startDate = row.startDate,
                endDate = row.endDate,
                error = row.error,
            ),
        )
    }

    private fun editEditor(edit: (PeriodEditorState) -> PeriodEditorState) = update { current ->
        val editor: PeriodEditorState = current.editor ?: return@update current
        current.copy(editor = edit(editor))
    }

    private fun confirmEditor() = update { current ->
        val editor: PeriodEditorState = current.editor ?: return@update current
        current.copy(
            editor = null,
            periods = current.periods.map { row ->
                if (row.ordinal == editor.ordinal) {
                    row.copy(startDate = editor.startDate, endDate = editor.endDate)
                } else {
                    row
                }
            },
        )
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

    private fun redivide(change: (SetupYearUiState) -> SetupYearUiState) = update { current ->
        val changed: SetupYearUiState = change(current)
        changed.copy(periods = dividePeriods(changed), editor = null)
    }

    private fun update(change: (SetupYearUiState) -> SetupYearUiState) {
        _state.value = validate(change(_state.value))
    }

    private fun emit(effect: SetupYearUiEffect) {
        viewModelScope.launch { _effects.send(effect) }
    }

    private fun validate(state: SetupYearUiState): SetupYearUiState {
        val errors: Map<Int, PeriodRangeError?> = errorsFor(state)
        val periods: List<PeriodDraftRow> = state.periods.map { row -> row.copy(error = errors[row.ordinal]) }
        val dateRangeError: SetupYearMessage? = dateRangeErrorFor(state)
        val yearLabelError: SetupYearMessage? = SetupYearMessage.MISSING_LABEL.takeIf { state.yearLabel.isBlank() }

        return state.copy(
            periods = periods,
            editor = state.editor?.let { editor -> editor.copy(error = editorErrorFor(editor, state)) },
            yearLabelError = yearLabelError,
            dateRangeError = dateRangeError,
            canContinue = yearLabelError == null &&
                dateRangeError == null &&
                periods.isNotEmpty() &&
                periods.none { it.error != null },
        )
    }

    private fun dateRangeErrorFor(state: SetupYearUiState): SetupYearMessage? {
        val startDate: LocalDate = state.startDate ?: return null
        val endDate: LocalDate = state.endDate ?: return null
        if (!endDate.isAfter(startDate)) return SetupYearMessage.INVALID_RANGE
        if (!isLongEnough(startDate, endDate, state.periodKind)) return SetupYearMessage.TOO_SHORT
        return null
    }

    private fun editorErrorFor(editor: PeriodEditorState, state: SetupYearUiState): PeriodRangeError? {
        val edited: PeriodDates = PeriodDates(editor.ordinal, editor.startDate, editor.endDate)
        return errorsFor(state, edited)[editor.ordinal]
    }

    private fun errorsFor(state: SetupYearUiState, replacement: PeriodDates? = null): Map<Int, PeriodRangeError?> {
        val startDate: LocalDate = state.startDate ?: return emptyMap()
        val endDate: LocalDate = state.endDate ?: return emptyMap()
        val ranges: List<PeriodDates> = state.periods.map { row ->
            if (replacement != null && row.ordinal == replacement.number) {
                replacement
            } else {
                PeriodDates(row.ordinal, row.startDate, row.endDate)
            }
        }
        return ranges.associate { period -> period.number to period.errorWithin(ranges, startDate, endDate) }
    }

    private fun isLongEnough(startDate: LocalDate, endDate: LocalDate, periodKind: PeriodKind): Boolean =
        ChronoUnit.DAYS.between(startDate, endDate) + 1 >= periodKind.periodCount

    private fun prefilledState(clock: Clock): SetupYearUiState {
        val prefill: SchoolYearPrefill = schoolYearPrefillFrom(clock)
        val state = SetupYearUiState(
            yearLabel = prefill.label,
            startDate = prefill.startDate,
            endDate = prefill.endDate,
        )
        return validate(state.copy(periods = dividePeriods(state)))
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
}
