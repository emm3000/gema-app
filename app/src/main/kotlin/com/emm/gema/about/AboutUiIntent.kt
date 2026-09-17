package com.emm.gema.about

sealed interface AboutUiIntent {

    data class SourceLinkClicked(val source: AboutSource) : AboutUiIntent

    data object BackClicked : AboutUiIntent
}
