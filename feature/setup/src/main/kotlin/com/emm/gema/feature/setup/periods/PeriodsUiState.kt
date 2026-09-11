package com.emm.gema.feature.setup.periods

import com.emm.gema.core.domain.schoolyear.PeriodId
import com.emm.gema.core.domain.schoolyear.PeriodKind
import com.emm.gema.feature.setup.PeriodRangeError
import java.time.LocalDate

data class PeriodsUiState(
    val isLoading: Boolean = true,
    val schoolYearLabel: String = "",
    val periodKind: PeriodKind = PeriodKind.BIMESTER,
    val periods: List<PeriodRow> = emptyList(),
    val overlapError: PeriodRangeError? = null,
    val overlappingStartIds: Set<PeriodId> = emptySet(),
    val overlappingEndIds: Set<PeriodId> = emptySet(),
    val canSave: Boolean = false,
)

data class PeriodRow(
    val id: PeriodId,
    val number: Int,
    val label: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val isCurrent: Boolean,
)
