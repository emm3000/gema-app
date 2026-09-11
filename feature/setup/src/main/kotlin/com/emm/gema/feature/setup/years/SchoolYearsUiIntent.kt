package com.emm.gema.feature.setup.years

sealed interface SchoolYearsUiIntent {

    data class YearClicked(val id: String) : SchoolYearsUiIntent

    data class PeriodsClicked(val id: String) : SchoolYearsUiIntent

    data object AddYearClicked : SchoolYearsUiIntent

    data object BackClicked : SchoolYearsUiIntent
}
