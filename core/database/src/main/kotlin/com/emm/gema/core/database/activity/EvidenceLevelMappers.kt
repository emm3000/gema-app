package com.emm.gema.core.database.activity

import com.emm.gema.core.database.Evidence_level as EvidenceLevelRow
import com.emm.gema.core.domain.activity.EvidenceLevel
import com.emm.gema.core.domain.activity.EvidenceLevelKey
import com.emm.gema.core.domain.evaluation.AchievementLevel

fun EvidenceLevelRow.toDomain(): EvidenceLevel = EvidenceLevel(
    key = EvidenceLevelKey(
        activityId = activity_id,
        studentId = student_id,
        competencyId = competency_id,
    ),
    achievementLevel = AchievementLevel.valueOf(achievement_level),
)
