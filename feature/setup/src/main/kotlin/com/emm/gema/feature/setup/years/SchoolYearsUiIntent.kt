package com.emm.gema.feature.setup.years

import com.emm.gema.core.domain.schoolyear.SchoolYearId

sealed interface SchoolYearsUiIntent {

    data class YearClicked(val id: SchoolYearId) : SchoolYearsUiIntent

    data class PeriodsClicked(val id: SchoolYearId) : SchoolYearsUiIntent

    data object AddYearClicked : SchoolYearsUiIntent

    data object BackClicked : SchoolYearsUiIntent
}
