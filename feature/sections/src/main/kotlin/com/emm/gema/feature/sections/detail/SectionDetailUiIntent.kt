package com.emm.gema.feature.sections.detail

sealed interface SectionDetailUiIntent {

    data object StudentsClicked : SectionDetailUiIntent

    data object AreasClicked : SectionDetailUiIntent

    data object RenameClicked : SectionDetailUiIntent

    data object BackClicked : SectionDetailUiIntent
}
