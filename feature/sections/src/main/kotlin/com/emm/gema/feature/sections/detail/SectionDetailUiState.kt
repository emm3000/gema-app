package com.emm.gema.feature.sections.detail

data class SectionDetailUiState(
    val isLoading: Boolean = true,
    val sectionTitle: String = "",
    val studentCount: Int = 0,
    val currentPeriodLabel: String? = null,
    val hasStoredTemplate: Boolean = false,
    val missingPeriodLevelCount: Int = 0,
    val activityCount: Int = 0,
    val todayLabel: String = "",
    val todayAttendanceSummary: String = "",
    val isTodayAttendanceTaken: Boolean = false,
)
