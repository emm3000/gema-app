package com.emm.gema.feature.setup.periods

sealed interface PeriodsUiEffect {

    data object NavigateBack : PeriodsUiEffect

    data class ShowMessage(val text: String) : PeriodsUiEffect
}
