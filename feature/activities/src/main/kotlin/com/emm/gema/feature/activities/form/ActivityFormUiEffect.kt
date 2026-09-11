package com.emm.gema.feature.activities.form

sealed interface ActivityFormUiEffect {

    data class NavigateToActivityEvidence(val activityId: String) : ActivityFormUiEffect

    data object NavigateBack : ActivityFormUiEffect

    data class ShowMessage(val message: ActivityFormMessage) : ActivityFormUiEffect
}
