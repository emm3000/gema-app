package com.emm.gema.feature.setup.years

import com.emm.gema.core.domain.schoolyear.PeriodKind
import com.emm.gema.core.domain.schoolyear.SchoolYearId
import java.time.LocalDate

data class SchoolYearsUiState(
    val isLoading: Boolean = true,
    val years: List<SchoolYearRow> = emptyList(),
)

data class SchoolYearRow(
    val id: SchoolYearId,
    val label: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val periodKind: PeriodKind,
    val sectionCount: Int,
    val isActive: Boolean,
)
