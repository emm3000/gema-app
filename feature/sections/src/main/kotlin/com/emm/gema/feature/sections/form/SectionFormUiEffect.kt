package com.emm.gema.feature.sections.form

sealed interface SectionFormUiEffect {

    data object NavigateBack : SectionFormUiEffect

    data class ShowMessage(val message: SectionFormMessage) : SectionFormUiEffect
}
