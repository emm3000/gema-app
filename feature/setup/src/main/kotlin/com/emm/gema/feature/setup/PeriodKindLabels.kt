package com.emm.gema.feature.setup

import com.emm.gema.core.domain.schoolyear.PeriodKind

fun PeriodKind.periodCountPlural(): Int = when (this) {
    PeriodKind.BIMESTER -> R.plurals.setup_periods_bimester_count
    PeriodKind.TRIMESTER -> R.plurals.setup_periods_trimester_count
}
