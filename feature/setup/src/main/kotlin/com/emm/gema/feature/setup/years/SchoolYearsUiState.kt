package com.emm.gema.feature.setup.years

import com.emm.gema.core.domain.schoolyear.SchoolYearId

data class SchoolYearsUiState(
    val isLoading: Boolean = true,
    val years: List<SchoolYearRow> = emptyList(),
)

data class SchoolYearRow(
    val id: SchoolYearId,
    val label: String,
    val dateRangeLabel: String,
    val periodKindLabel: String,
    val sectionCount: Int,
    val isActive: Boolean,
)
