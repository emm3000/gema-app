package com.emm.gema.feature.activities.evidence

sealed interface ActivityEvidenceUiEffect {

    data class NavigateToActivityForm(val sectionId: String, val activityId: String?) : ActivityEvidenceUiEffect

    data object NavigateBack : ActivityEvidenceUiEffect

    data class ShowMessage(val text: String) : ActivityEvidenceUiEffect
}
