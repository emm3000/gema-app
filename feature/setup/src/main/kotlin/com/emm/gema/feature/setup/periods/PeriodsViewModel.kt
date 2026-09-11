package com.emm.gema.feature.setup.periods

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.gema.core.domain.schoolyear.GetCurrentPeriodUseCase
import com.emm.gema.core.domain.schoolyear.GetPeriodsUseCase
import com.emm.gema.core.domain.schoolyear.GetSchoolYearUseCase
import com.emm.gema.core.domain.schoolyear.Period
import com.emm.gema.core.domain.schoolyear.PeriodDates
import com.emm.gema.core.domain.schoolyear.PeriodId
import com.emm.gema.core.domain.schoolyear.SchoolYear
import com.emm.gema.core.domain.schoolyear.SchoolYearId
import com.emm.gema.core.domain.schoolyear.UpdatePeriodsUseCase
import com.emm.gema.core.domain.schoolyear.kindLabel
import com.emm.gema.core.domain.schoolyear.labelFor
import com.emm.gema.feature.setup.PeriodRangeError
import com.emm.gema.feature.setup.errorWithin
import java.time.LocalDate
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class PeriodsViewModel(
    private val schoolYearId: SchoolYearId,
    private val getSchoolYear: GetSchoolYearUseCase,
    private val getPeriods: GetPeriodsUseCase,
    private val getCurrentPeriod: GetCurrentPeriodUseCase,
    private val updatePeriods: UpdatePeriodsUseCase,
) : ViewModel() {

    private val _state: MutableStateFlow<PeriodsUiState> = MutableStateFlow(PeriodsUiState())
    val state: StateFlow<PeriodsUiState> = _state.asStateFlow()

    private val _effects: Channel<PeriodsUiEffect> = Channel(Channel.BUFFERED)
    val effects: Flow<PeriodsUiEffect> = _effects.receiveAsFlow()

    private var schoolYear: SchoolYear? = null

    init {
        viewModelScope.launch { load() }
    }

    fun onIntent(intent: PeriodsUiIntent) {
        when (intent) {
            is PeriodsUiIntent.StartDateChanged -> editRow(intent.id) { it.copy(startDate = intent.value) }
            is PeriodsUiIntent.EndDateChanged -> editRow(intent.id) { it.copy(endDate = intent.value) }
            PeriodsUiIntent.SaveClicked -> save()
            PeriodsUiIntent.BackClicked -> emit(PeriodsUiEffect.NavigateBack)
        }
    }

    private suspend fun load() {
        val loaded: SchoolYear = getSchoolYear(schoolYearId) ?: return
        schoolYear = loaded
        val currentPeriodId: PeriodId? = getCurrentPeriod(schoolYearId)?.id

        getPeriods(schoolYearId).collect { periods ->
            _state.value = validate(
                _state.value.copy(
                    isLoading = false,
                    schoolYearLabel = loaded.label,
                    periodKindLabel = loaded.periodKind.kindLabel(),
                    periods = periods.map { it.toRow(loaded, currentPeriodId) },
                )
            )
        }
    }

    private fun Period.toRow(schoolYear: SchoolYear, currentPeriodId: PeriodId?): PeriodRow = PeriodRow(
        id = id,
        number = number,
        label = schoolYear.periodKind.labelFor(number),
        startDate = startDate,
        endDate = endDate,
        isCurrent = id == currentPeriodId,
    )

    private fun editRow(id: PeriodId, edit: (PeriodRow) -> PeriodRow) {
        _state.value = validate(
            _state.value.copy(
                periods = _state.value.periods.map { row -> if (row.id == id) edit(row) else row },
            )
        )
    }

    private fun save() {
        val current: PeriodsUiState = _state.value
        if (!current.canSave) return

        viewModelScope.launch {
            runCatching { updatePeriods(schoolYearId, current.periods.map { it.toDates() }) }
                .onSuccess { _effects.send(PeriodsUiEffect.NavigateBack) }
                .onFailure { _effects.send(PeriodsUiEffect.ShowMessage(PeriodsMessage.SAVE_FAILED)) }
        }
    }

    private fun emit(effect: PeriodsUiEffect) {
        viewModelScope.launch { _effects.send(effect) }
    }

    private fun validate(state: PeriodsUiState): PeriodsUiState {
        val yearStart: LocalDate = schoolYear?.startDate ?: return state
        val yearEnd: LocalDate = schoolYear?.endDate ?: return state
        val ranges: List<PeriodDates> = state.periods.map { it.toDates() }
        val errorsById: Map<PeriodId, PeriodRangeError?> = state.periods.associate { row ->
            row.id to row.toDates().errorWithin(ranges, yearStart, yearEnd)
        }
        val overlapError: PeriodRangeError? = errorsById.values.firstNotNullOfOrNull { it }
        val overlappingEdges: OverlappingEdges = overlappingEdgesFor(state.periods)

        return state.copy(
            overlapError = overlapError,
            overlappingStartIds = overlappingEdges.startIds,
            overlappingEndIds = overlappingEdges.endIds,
            canSave = overlapError == null && state.periods.isNotEmpty(),
        )
    }

    private fun overlappingEdgesFor(periods: List<PeriodRow>): OverlappingEdges {
        val overlappingPairs: List<Pair<PeriodRow, PeriodRow>> = periods
            .flatMap { first -> periods.map { second -> first to second } }
            .filter { (first: PeriodRow, second: PeriodRow) -> first.id != second.id && first.overlapsWith(second) }

        val overlappingStartIds: Set<PeriodId> = overlappingPairs
            .map { (first: PeriodRow, second: PeriodRow) -> if (first.startDate <= second.startDate) second else first }
            .map { it.id }
            .toSet()
        val overlappingEndIds: Set<PeriodId> = overlappingPairs
            .map { (first: PeriodRow, second: PeriodRow) -> if (first.startDate <= second.startDate) first else second }
            .map { it.id }
            .toSet()

        return OverlappingEdges(startIds = overlappingStartIds, endIds = overlappingEndIds)
    }

    private fun PeriodRow.overlapsWith(other: PeriodRow): Boolean =
        startDate <= other.endDate && other.startDate <= endDate

    private data class OverlappingEdges(
        val startIds: Set<PeriodId>,
        val endIds: Set<PeriodId>,
    )

    private fun PeriodRow.toDates(): PeriodDates = PeriodDates(number, startDate, endDate)
}
