package com.emm.gema.feature.setup.periods

import com.emm.gema.core.domain.schoolyear.PeriodId
import java.time.LocalDate

sealed interface PeriodsUiIntent {

    data class StartDateChanged(val id: PeriodId, val value: LocalDate) : PeriodsUiIntent

    data class EndDateChanged(val id: PeriodId, val value: LocalDate) : PeriodsUiIntent

    data object SaveClicked : PeriodsUiIntent

    data object BackClicked : PeriodsUiIntent
}
