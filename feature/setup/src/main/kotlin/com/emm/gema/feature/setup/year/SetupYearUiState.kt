package com.emm.gema.feature.setup.year

import com.emm.gema.core.domain.schoolyear.PeriodKind
import java.time.LocalDate

data class SetupYearUiState(
    val isLoading: Boolean = false,
    val yearLabel: String = "",
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val periodKind: PeriodKind = PeriodKind.BIMESTER,
    val periods: List<PeriodDraftRow> = emptyList(),
    val yearLabelError: String? = null,
    val dateRangeError: String? = null,
    val canContinue: Boolean = false,
)

data class PeriodDraftRow(
    val ordinal: Int,
    val label: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val error: String?,
)
