package com.emm.gema.feature.setup.section

sealed interface SetupSectionUiEffect {

    data object NavigateToHome : SetupSectionUiEffect

    data class NavigateToSectionAreas(val sectionId: String) : SetupSectionUiEffect

    data object NavigateBack : SetupSectionUiEffect

    data class ShowMessage(val message: SetupSectionMessage) : SetupSectionUiEffect
}
