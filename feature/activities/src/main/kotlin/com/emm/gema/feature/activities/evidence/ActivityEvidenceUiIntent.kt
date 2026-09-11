package com.emm.gema.feature.activities.evidence

import com.emm.gema.core.domain.activity.EvidenceMark
import com.emm.gema.core.domain.curriculum.CompetencyId
import com.emm.gema.core.domain.student.StudentId

sealed interface ActivityEvidenceUiIntent {

    data class CompetencySelected(val id: CompetencyId) : ActivityEvidenceUiIntent

    data class LevelSelected(val studentId: StudentId, val mark: EvidenceMark?) : ActivityEvidenceUiIntent

    data object EditActivityClicked : ActivityEvidenceUiIntent

    data object BackClicked : ActivityEvidenceUiIntent
}
