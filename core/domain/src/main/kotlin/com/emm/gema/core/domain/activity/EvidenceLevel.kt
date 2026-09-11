package com.emm.gema.core.domain.activity

import com.emm.gema.core.domain.evaluation.AchievementLevel

data class EvidenceLevel(
    val key: EvidenceLevelKey,
    val achievementLevel: AchievementLevel,
)
