package com.emm.gema.about

sealed interface AboutUiEffect {

    data class OpenUrl(val url: String) : AboutUiEffect

    data object NavigateBack : AboutUiEffect
}
