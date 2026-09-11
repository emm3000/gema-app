package com.emm.gema.feature.activities.list

sealed interface ActivitiesUiIntent {

    data class PeriodSelected(val id: String) : ActivitiesUiIntent

    data class ActivityClicked(val id: String) : ActivitiesUiIntent

    data object AddActivityClicked : ActivitiesUiIntent

    data object BackClicked : ActivitiesUiIntent
}
