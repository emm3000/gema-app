package com.emm.gema.feature.setup.year

import com.emm.gema.core.domain.schoolyear.PeriodDates
import com.emm.gema.core.domain.schoolyear.PeriodKind
import java.time.LocalDate

data class SchoolYearDraft(
    val label: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val periodKind: PeriodKind,
    val periods: List<PeriodDates>,
)
