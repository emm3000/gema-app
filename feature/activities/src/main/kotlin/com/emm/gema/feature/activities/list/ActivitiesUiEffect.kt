package com.emm.gema.feature.activities.list

import com.emm.gema.core.domain.activity.ActivityId
import com.emm.gema.core.domain.section.SectionId

sealed interface ActivitiesUiEffect {

    data class NavigateToActivityEvidence(val activityId: ActivityId) : ActivitiesUiEffect

    data class NavigateToActivityForm(val sectionId: SectionId, val activityId: ActivityId?) : ActivitiesUiEffect

    data object NavigateBack : ActivitiesUiEffect
}
