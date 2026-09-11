package com.emm.gema.feature.evaluation.worked

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.gema.core.domain.curriculum.GetPeriodCompetenciesUseCase
import com.emm.gema.core.domain.curriculum.PeriodCompetency
import com.emm.gema.core.domain.curriculum.SetCompetencyWorkedUseCase
import com.emm.gema.core.domain.schoolyear.GetPeriodUseCase
import com.emm.gema.core.domain.schoolyear.GetSchoolYearUseCase
import com.emm.gema.core.domain.schoolyear.Period
import com.emm.gema.core.domain.schoolyear.SchoolYear
import com.emm.gema.core.domain.section.Area
import com.emm.gema.core.domain.section.GetSectionUseCase
import com.emm.gema.core.domain.section.Section
import com.emm.gema.feature.evaluation.labelFor
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

private const val TOGGLE_FAILED_MESSAGE: String = "No se pudo guardar el cambio"

class WorkedCompetenciesViewModel(
    private val sectionId: String,
    private val periodId: String,
    private val area: Area,
    private val getSection: GetSectionUseCase,
    private val getPeriod: GetPeriodUseCase,
    private val getSchoolYear: GetSchoolYearUseCase,
    private val getPeriodCompetencies: GetPeriodCompetenciesUseCase,
    private val setCompetencyWorked: SetCompetencyWorkedUseCase,
) : ViewModel() {

    private val _state: MutableStateFlow<WorkedCompetenciesUiState> =
        MutableStateFlow(WorkedCompetenciesUiState())
    val state: StateFlow<WorkedCompetenciesUiState> = _state.asStateFlow()

    private val _effects: Channel<WorkedCompetenciesUiEffect> = Channel(Channel.BUFFERED)
    val effects: Flow<WorkedCompetenciesUiEffect> = _effects.receiveAsFlow()

    init {
        viewModelScope.launch { load() }
    }

    fun onIntent(intent: WorkedCompetenciesUiIntent) {
        when (intent) {
            is WorkedCompetenciesUiIntent.CompetencyToggled -> toggle(intent.id, intent.isWorked)
            WorkedCompetenciesUiIntent.BackClicked -> emit(WorkedCompetenciesUiEffect.NavigateBack)
        }
    }

    private suspend fun load() {
        val periodLabel: String = periodLabel()

        getPeriodCompetencies(sectionId = sectionId, periodId = periodId, area = area).collect { competencies ->
            _state.value = _state.value.copy(
                isLoading = false,
                areaName = area.officialName,
                periodLabel = periodLabel,
                competencies = competencies.map { it.toRow() },
                selectedCount = competencies.count { it.isWorked },
            )
        }
    }

    private suspend fun periodLabel(): String {
        val section: Section = getSection(sectionId) ?: return ""
        val period: Period = getPeriod(periodId) ?: return ""
        val schoolYear: SchoolYear = getSchoolYear(section.schoolYearId) ?: return ""

        return schoolYear.periodKind.labelFor(period.number)
    }

    private fun toggle(competencyId: String, isWorked: Boolean) {
        viewModelScope.launch {
            runCatching {
                setCompetencyWorked(
                    sectionId = sectionId,
                    periodId = periodId,
                    competencyId = competencyId,
                    isWorked = isWorked,
                )
            }.onFailure { _effects.send(WorkedCompetenciesUiEffect.ShowMessage(TOGGLE_FAILED_MESSAGE)) }
        }
    }

    private fun emit(effect: WorkedCompetenciesUiEffect) {
        viewModelScope.launch { _effects.send(effect) }
    }

    private fun PeriodCompetency.toRow(): CompetencyToggleRow = CompetencyToggleRow(
        id = competency.id,
        siagieOrdinal = competency.siagieOrdinal,
        name = competency.name,
        isWorked = isWorked,
        recordedLevelCount = 0,
    )
}
