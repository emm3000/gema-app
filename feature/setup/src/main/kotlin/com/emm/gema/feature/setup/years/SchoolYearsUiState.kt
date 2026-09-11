package com.emm.gema.feature.setup.years

data class SchoolYearsUiState(
    val isLoading: Boolean = true,
    val years: List<SchoolYearRow> = emptyList(),
)

data class SchoolYearRow(
    val id: String,
    val label: String,
    val dateRangeLabel: String,
    val periodKindLabel: String,
    val sectionCount: Int,
    val isActive: Boolean,
)
