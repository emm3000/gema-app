package com.emm.gema.feature.setup.periods

import java.time.LocalDate

data class PeriodsUiState(
    val isLoading: Boolean = true,
    val schoolYearLabel: String = "",
    val periodKindLabel: String = "",
    val periods: List<PeriodRow> = emptyList(),
    val overlapError: String? = null,
    val canSave: Boolean = false,
)

data class PeriodRow(
    val id: String,
    val number: Int,
    val label: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val isCurrent: Boolean,
)
