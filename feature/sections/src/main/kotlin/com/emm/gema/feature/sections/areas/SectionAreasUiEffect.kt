package com.emm.gema.feature.sections.areas

sealed interface SectionAreasUiEffect {

    data object NavigateBack : SectionAreasUiEffect

    data class ShowMessage(val text: String) : SectionAreasUiEffect
}
