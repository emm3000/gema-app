package com.emm.gema.feature.sections.detail

sealed interface SectionDetailUiEffect {

    data class NavigateToStudents(val sectionId: String) : SectionDetailUiEffect

    data class NavigateToSectionAreas(val sectionId: String) : SectionDetailUiEffect

    data class NavigateToSectionForm(val schoolYearId: String, val sectionId: String) : SectionDetailUiEffect

    data object NavigateBack : SectionDetailUiEffect
}
