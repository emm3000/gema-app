package com.emm.gema.core.domain.activity

import com.emm.gema.core.domain.evaluation.AchievementLevel
import java.time.LocalDate

data class EvidenceLevel(
    val key: EvidenceLevelKey,
    val achievementLevel: AchievementLevel?,
)

data class EvidenceRecord(
    val activityId: ActivityId,
    val activityName: String,
    val date: LocalDate,
    val achievementLevel: AchievementLevel,
)
