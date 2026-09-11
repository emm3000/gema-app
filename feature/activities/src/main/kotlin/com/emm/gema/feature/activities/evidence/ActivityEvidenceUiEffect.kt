package com.emm.gema.feature.activities.evidence

import com.emm.gema.core.domain.activity.ActivityId
import com.emm.gema.core.domain.section.SectionId

sealed interface ActivityEvidenceUiEffect {

    data class NavigateToActivityForm(val sectionId: SectionId, val activityId: ActivityId?) : ActivityEvidenceUiEffect

    data object NavigateBack : ActivityEvidenceUiEffect

    data class ShowMessage(val message: ActivityEvidenceMessage) : ActivityEvidenceUiEffect
}
