package com.emm.gema.feature.activities.form

import com.emm.gema.core.domain.activity.ActivityId

sealed interface ActivityFormUiEffect {

    data class NavigateToActivityEvidence(val activityId: ActivityId) : ActivityFormUiEffect

    data object NavigateBack : ActivityFormUiEffect

    data class ShowMessage(val message: ActivityFormMessage) : ActivityFormUiEffect
}
