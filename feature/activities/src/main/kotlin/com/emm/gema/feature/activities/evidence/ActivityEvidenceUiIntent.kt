package com.emm.gema.feature.activities.evidence

import com.emm.gema.core.domain.evaluation.AchievementLevel

sealed interface ActivityEvidenceUiIntent {

    data class CompetencySelected(val id: String) : ActivityEvidenceUiIntent

    data class LevelSelected(val studentId: String, val level: AchievementLevel?) : ActivityEvidenceUiIntent

    data object EditActivityClicked : ActivityEvidenceUiIntent

    data object BackClicked : ActivityEvidenceUiIntent
}
