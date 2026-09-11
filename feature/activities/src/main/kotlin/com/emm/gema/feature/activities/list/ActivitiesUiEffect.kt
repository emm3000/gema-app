package com.emm.gema.feature.activities.list

sealed interface ActivitiesUiEffect {

    data class NavigateToActivityEvidence(val activityId: String) : ActivitiesUiEffect

    data class NavigateToActivityForm(val sectionId: String, val activityId: String?) : ActivitiesUiEffect

    data object NavigateBack : ActivitiesUiEffect
}
