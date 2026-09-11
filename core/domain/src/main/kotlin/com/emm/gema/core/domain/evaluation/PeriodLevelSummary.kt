package com.emm.gema.core.domain.evaluation

import com.emm.gema.core.domain.section.Area

data class PeriodLevelSummary(
    val areas: List<PeriodLevelAreaSummary>,
)

data class PeriodLevelAreaSummary(
    val area: Area,
    val grid: PeriodLevelGrid,
)
