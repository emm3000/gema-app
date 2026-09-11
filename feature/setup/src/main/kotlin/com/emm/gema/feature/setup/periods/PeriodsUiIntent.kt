package com.emm.gema.feature.setup.periods

import java.time.LocalDate

sealed interface PeriodsUiIntent {

    data class StartDateChanged(val id: String, val value: LocalDate) : PeriodsUiIntent

    data class EndDateChanged(val id: String, val value: LocalDate) : PeriodsUiIntent

    data object SaveClicked : PeriodsUiIntent

    data object BackClicked : PeriodsUiIntent
}
