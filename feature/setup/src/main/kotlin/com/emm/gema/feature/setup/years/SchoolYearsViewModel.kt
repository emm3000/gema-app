package com.emm.gema.feature.setup.years

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.gema.core.domain.schoolyear.GetActiveSchoolYearUseCase
import com.emm.gema.core.domain.schoolyear.GetSchoolYearsUseCase
import com.emm.gema.core.domain.schoolyear.SchoolYear
import com.emm.gema.core.domain.schoolyear.SwitchSchoolYearUseCase
import com.emm.gema.core.domain.section.GetSectionCountsUseCase
import com.emm.gema.core.domain.schoolyear.kindLabel
import com.emm.gema.feature.setup.rangeLabel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class SchoolYearsViewModel(
    getSchoolYears: GetSchoolYearsUseCase,
    getSectionCounts: GetSectionCountsUseCase,
    getActiveSchoolYear: GetActiveSchoolYearUseCase,
    private val switchSchoolYear: SwitchSchoolYearUseCase,
) : ViewModel() {

    private val _state: MutableStateFlow<SchoolYearsUiState> = MutableStateFlow(SchoolYearsUiState())
    val state: StateFlow<SchoolYearsUiState> = _state.asStateFlow()

    private val _effects: Channel<SchoolYearsUiEffect> = Channel(Channel.BUFFERED)
    val effects: Flow<SchoolYearsUiEffect> = _effects.receiveAsFlow()

    init {
        viewModelScope.launch {
            combine(
                getSchoolYears(),
                getSectionCounts(),
                getActiveSchoolYear(),
            ) { schoolYears, sectionCounts, activeSchoolYear ->
                SchoolYearsUiState(
                    isLoading = false,
                    years = schoolYears.map { it.toRow(sectionCounts, activeSchoolYear) },
                )
            }.collect { _state.value = it }
        }
    }

    fun onIntent(intent: SchoolYearsUiIntent) {
        when (intent) {
            is SchoolYearsUiIntent.YearClicked -> viewModelScope.launch { switchSchoolYear(intent.id) }
            is SchoolYearsUiIntent.PeriodsClicked -> emit(SchoolYearsUiEffect.NavigateToPeriods(intent.id))
            SchoolYearsUiIntent.AddYearClicked -> emit(SchoolYearsUiEffect.NavigateToSetupYear)
            SchoolYearsUiIntent.BackClicked -> emit(SchoolYearsUiEffect.NavigateBack)
        }
    }

    private fun emit(effect: SchoolYearsUiEffect) {
        viewModelScope.launch { _effects.send(effect) }
    }

    private fun SchoolYear.toRow(sectionCounts: Map<String, Int>, activeSchoolYear: SchoolYear?): SchoolYearRow =
        SchoolYearRow(
            id = id,
            label = label,
            dateRangeLabel = rangeLabel(startDate, endDate),
            periodKindLabel = periodKind.kindLabel(),
            sectionCount = sectionCounts[id] ?: 0,
            isActive = id == activeSchoolYear?.id,
        )
}
