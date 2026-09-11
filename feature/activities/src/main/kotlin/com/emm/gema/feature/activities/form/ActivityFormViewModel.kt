package com.emm.gema.feature.activities.form

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.gema.core.domain.activity.Activity
import com.emm.gema.core.domain.activity.DeleteActivityUseCase
import com.emm.gema.core.domain.activity.GetActivityUseCase
import com.emm.gema.core.domain.activity.SaveActivityResult
import com.emm.gema.core.domain.activity.SaveActivityUseCase
import com.emm.gema.core.domain.curriculum.Competency
import com.emm.gema.core.domain.curriculum.GetWorkedCompetenciesUseCase
import com.emm.gema.core.domain.schoolyear.FindPeriodForDateUseCase
import com.emm.gema.core.domain.schoolyear.GetSchoolYearUseCase
import com.emm.gema.core.domain.schoolyear.Period
import com.emm.gema.core.domain.schoolyear.SchoolYear
import com.emm.gema.core.domain.schoolyear.labelFor
import com.emm.gema.core.domain.section.GetSectionUseCase
import com.emm.gema.core.domain.section.Section
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate

class ActivityFormViewModel(
    private val sectionId: String,
    private val activityId: String?,
    private val clock: Clock = Clock.systemDefaultZone(),
    private val getSection: GetSectionUseCase,
    private val getSchoolYear: GetSchoolYearUseCase,
    private val findPeriodForDate: FindPeriodForDateUseCase,
    private val getWorkedCompetencies: GetWorkedCompetenciesUseCase,
    private val getActivity: GetActivityUseCase,
    private val saveActivity: SaveActivityUseCase,
    private val deleteActivity: DeleteActivityUseCase,
) : ViewModel() {

    private val _state: MutableStateFlow<ActivityFormUiState> =
        MutableStateFlow(ActivityFormUiState(activityId = activityId))
    val state: StateFlow<ActivityFormUiState> = _state.asStateFlow()

    private val _effects: Channel<ActivityFormUiEffect> = Channel(Channel.BUFFERED)
    val effects: Flow<ActivityFormUiEffect> = _effects.receiveAsFlow()

    private var section: Section? = null
    private var schoolYear: SchoolYear? = null
    private var originalPeriodId: String? = null

    init {
        viewModelScope.launch { load() }
    }

    fun onIntent(intent: ActivityFormUiIntent) {
        when (intent) {
            is ActivityFormUiIntent.NameChanged -> _state.value = _state.value.copy(name = intent.value)
            is ActivityFormUiIntent.DateChanged -> viewModelScope.launch { changeDate(intent.value) }
            is ActivityFormUiIntent.CompetencyToggled -> toggleCompetency(intent.id, intent.isSelected)
            ActivityFormUiIntent.SaveClicked -> save()
            ActivityFormUiIntent.DeleteClicked -> _state.value = _state.value.copy(isDeleteConfirmVisible = true)
            ActivityFormUiIntent.DeleteConfirmed -> delete()
            ActivityFormUiIntent.DeleteDismissed -> _state.value = _state.value.copy(isDeleteConfirmVisible = false)
            ActivityFormUiIntent.BackClicked -> emit(ActivityFormUiEffect.NavigateBack)
        }
    }

    private suspend fun load() {
        val loadedSection: Section = getSection(sectionId) ?: return
        val loadedSchoolYear: SchoolYear = getSchoolYear(loadedSection.schoolYearId) ?: return
        section = loadedSection
        schoolYear = loadedSchoolYear

        val activity: Activity? = activityId?.let { getActivity(it) }
        originalPeriodId = activity?.periodId
        val date: LocalDate = activity?.date ?: LocalDate.now(clock)

        _state.value = _state.value.copy(
            isLoading = false,
            name = activity?.name.orEmpty(),
            date = date,
            selectedCompetencyIds = activity?.competencyIds.orEmpty(),
        )
        resolvePeriod(date)
    }

    private suspend fun changeDate(date: LocalDate) {
        _state.value = _state.value.copy(date = date)
        resolvePeriod(date)
    }

    private suspend fun resolvePeriod(date: LocalDate) {
        val loadedSchoolYear: SchoolYear = schoolYear ?: return
        val period: Period? = findPeriodForDate(loadedSchoolYear.id, date)

        if (period == null) {
            _state.value = _state.value.copy(
                dateError = ActivityFormMessage.OUTSIDE_PERIODS,
                resolvedPeriodLabel = null,
                hasPeriodChangeWarning = false,
                competencyGroups = emptyList(),
                selectedCompetencyIds = emptySet(),
            )
            return
        }

        val competencies: List<Competency> = getWorkedCompetencies(sectionId, period.id).first()
        val availableIds: Set<String> = competencies.map { it.id }.toSet()

        _state.value = _state.value.copy(
            dateError = null,
            resolvedPeriodLabel = loadedSchoolYear.periodKind.labelFor(period.number),
            hasPeriodChangeWarning = hasPeriodChanged(period.id),
            competencyGroups = competencies.toGroups(),
            selectedCompetencyIds = _state.value.selectedCompetencyIds.filter { it in availableIds }.toSet(),
        )
    }

    private fun hasPeriodChanged(resolvedPeriodId: String): Boolean {
        val original: String = originalPeriodId ?: return false
        return original != resolvedPeriodId
    }

    private fun toggleCompetency(competencyId: String, isSelected: Boolean) {
        val current: Set<String> = _state.value.selectedCompetencyIds
        _state.value = _state.value.copy(
            selectedCompetencyIds = if (isSelected) current + competencyId else current - competencyId,
        )
    }

    private fun save() {
        val current: ActivityFormUiState = _state.value
        val date: LocalDate = current.date ?: return
        if (!current.canSave) return

        viewModelScope.launch {
            when (
                val result: SaveActivityResult = saveActivity(
                    sectionId = sectionId,
                    activityId = activityId,
                    name = current.name,
                    date = date,
                    competencyIds = current.selectedCompetencyIds,
                )
            ) {
                is SaveActivityResult.Saved -> emit(ActivityFormUiEffect.NavigateToActivityEvidence(result.activity.id))
                SaveActivityResult.DateOutsidePeriods ->
                    _state.value = _state.value.copy(dateError = ActivityFormMessage.OUTSIDE_PERIODS)
            }
        }
    }

    private fun delete() {
        val id: String = activityId ?: return

        viewModelScope.launch {
            runCatching { deleteActivity(id) }
                .onSuccess { emit(ActivityFormUiEffect.NavigateBack) }
                .onFailure { emit(ActivityFormUiEffect.ShowMessage(ActivityFormMessage.SAVE_FAILED)) }
        }
    }

    private fun emit(effect: ActivityFormUiEffect) {
        viewModelScope.launch { _effects.send(effect) }
    }
}

private fun List<Competency>.toGroups(): List<CompetencyGroup> = groupBy { it.area }
    .toList()
    .sortedBy { (area, _) -> area.ordinal }
    .map { (area, competencies) ->
        CompetencyGroup(
            areaName = area.officialName,
            competencies = competencies.sortedBy { it.siagieOrdinal }.map {
                CompetencyToggleRow(id = it.id, siagieOrdinal = it.siagieOrdinal, name = it.name)
            },
        )
    }
