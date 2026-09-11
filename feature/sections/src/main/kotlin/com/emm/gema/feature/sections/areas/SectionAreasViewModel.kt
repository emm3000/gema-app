package com.emm.gema.feature.sections.areas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.gema.core.domain.evaluation.GetAreaRecordedLevelCountsUseCase
import com.emm.gema.core.domain.section.Area
import com.emm.gema.core.domain.section.GetSectionAreasUseCase
import com.emm.gema.core.domain.section.GetSectionUseCase
import com.emm.gema.core.domain.section.Section
import com.emm.gema.core.domain.section.SectionArea
import com.emm.gema.core.domain.section.SectionId
import com.emm.gema.core.domain.section.SetAreaVisibilityUseCase
import com.emm.gema.core.domain.section.title
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class SectionAreasViewModel(
    private val sectionId: SectionId,
    private val getSection: GetSectionUseCase,
    private val getSectionAreas: GetSectionAreasUseCase,
    private val setAreaVisibility: SetAreaVisibilityUseCase,
    private val getAreaRecordedLevelCounts: GetAreaRecordedLevelCountsUseCase,
) : ViewModel() {

    private val _state: MutableStateFlow<SectionAreasUiState> = MutableStateFlow(SectionAreasUiState())
    val state: StateFlow<SectionAreasUiState> = _state.asStateFlow()

    private val _effects: Channel<SectionAreasUiEffect> = Channel(Channel.BUFFERED)
    val effects: Flow<SectionAreasUiEffect> = _effects.receiveAsFlow()

    init {
        viewModelScope.launch { load() }
    }

    fun onIntent(intent: SectionAreasUiIntent) {
        when (intent) {
            is SectionAreasUiIntent.AreaToggled -> toggle(intent.id, intent.isActive)
            SectionAreasUiIntent.BackClicked -> emit(SectionAreasUiEffect.NavigateBack)
        }
    }

    private suspend fun load() {
        val section: Section? = getSection(sectionId)

        combine(
            getSectionAreas(sectionId),
            getAreaRecordedLevelCounts(sectionId),
        ) { areas: List<SectionArea>, counts: Map<Area, Int> ->
            _state.value.copy(
                isLoading = false,
                sectionTitle = section?.title().orEmpty(),
                areas = areas.map { it.toRow(counts[it.area] ?: 0) },
            )
        }.collect { _state.value = it }
    }

    private fun toggle(area: Area, isActive: Boolean) {
        viewModelScope.launch {
            runCatching { setAreaVisibility(sectionId = sectionId, area = area, isActive = isActive) }
                .onFailure { _effects.send(SectionAreasUiEffect.ShowMessage(SectionAreasMessage.TOGGLE_FAILED)) }
        }
    }

    private fun emit(effect: SectionAreasUiEffect) {
        viewModelScope.launch { _effects.send(effect) }
    }

    private fun SectionArea.toRow(recordedLevelCount: Int): AreaToggleRow = AreaToggleRow(
        id = area,
        name = area.officialName,
        isActive = isActive,
        recordedLevelCount = recordedLevelCount,
    )
}
