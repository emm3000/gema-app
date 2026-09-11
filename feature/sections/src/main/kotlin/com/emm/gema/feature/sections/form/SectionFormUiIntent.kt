package com.emm.gema.feature.sections.form

import com.emm.gema.core.domain.section.Grade

sealed interface SectionFormUiIntent {

    data class GradeSelected(val grade: Grade) : SectionFormUiIntent

    data class SectionNameChanged(val value: String) : SectionFormUiIntent

    data object SaveClicked : SectionFormUiIntent

    data object DeleteClicked : SectionFormUiIntent

    data object DeleteConfirmed : SectionFormUiIntent

    data object DeleteDismissed : SectionFormUiIntent

    data object BackClicked : SectionFormUiIntent
}
