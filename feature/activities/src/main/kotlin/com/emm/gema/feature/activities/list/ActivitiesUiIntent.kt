package com.emm.gema.feature.activities.list

import com.emm.gema.core.domain.activity.ActivityId
import com.emm.gema.core.domain.schoolyear.PeriodId

sealed interface ActivitiesUiIntent {

    data class PeriodSelected(val id: PeriodId) : ActivitiesUiIntent

    data class ActivityClicked(val id: ActivityId) : ActivitiesUiIntent

    data object AddActivityClicked : ActivitiesUiIntent

    data object BackClicked : ActivitiesUiIntent
}
