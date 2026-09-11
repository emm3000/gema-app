package com.emm.gema.feature.sections.areas

import com.emm.gema.core.domain.section.Area

data class SectionAreasUiState(
    val isLoading: Boolean = true,
    val sectionTitle: String = "",
    val areas: List<AreaToggleRow> = emptyList(),
)

data class AreaToggleRow(
    val id: Area,
    val name: String,
    val isActive: Boolean,
    val recordedLevelCount: Int,
)
