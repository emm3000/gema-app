package com.emm.gema.feature.setup.years

sealed interface SchoolYearsUiEffect {

    data class NavigateToPeriods(val id: String) : SchoolYearsUiEffect

    data object NavigateToSetupYear : SchoolYearsUiEffect

    data object NavigateBack : SchoolYearsUiEffect
}
