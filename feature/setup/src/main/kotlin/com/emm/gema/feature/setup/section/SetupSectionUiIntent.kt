package com.emm.gema.feature.setup.section

import com.emm.gema.core.domain.section.Grade

sealed interface SetupSectionUiIntent {

    data class GradeSelected(val grade: Grade) : SetupSectionUiIntent

    data class SectionNameChanged(val value: String) : SetupSectionUiIntent

    data object AreaSelectionClicked : SetupSectionUiIntent

    data object FinishClicked : SetupSectionUiIntent

    data object BackClicked : SetupSectionUiIntent
}
