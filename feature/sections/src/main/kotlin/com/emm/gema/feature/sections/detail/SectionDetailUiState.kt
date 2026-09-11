package com.emm.gema.feature.sections.detail

import java.time.LocalDate

data class SectionDetailUiState(
    val isLoading: Boolean = true,
    val sectionTitle: String = "",
    val studentCount: Int = 0,
    val currentPeriodLabel: String? = null,
    val missingPeriodLevelCount: Int = 0,
    val today: LocalDate? = null,
    val todayLabel: String = "",
    val todayAttendanceSummary: String = "",
)
