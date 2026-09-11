package com.emm.gema.feature.setup.section

import com.emm.gema.core.domain.section.SectionId

sealed interface SetupSectionUiEffect {

    data object NavigateToHome : SetupSectionUiEffect

    data class NavigateToSectionAreas(val sectionId: SectionId) : SetupSectionUiEffect

    data object NavigateBack : SetupSectionUiEffect

    data class ShowMessage(val message: SetupSectionMessage) : SetupSectionUiEffect
}
