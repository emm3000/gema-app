package com.emm.gema.feature.sections.detail

data class SectionDetailUiState(
    val isLoading: Boolean = true,
    val sectionTitle: String = "",
    val studentCount: Int = 0,
    val currentPeriodLabel: String? = null,
    val missingPeriodLevelCount: Int = 0,
)
