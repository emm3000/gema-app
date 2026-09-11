package com.emm.gema.core.domain.activity

import com.emm.gema.core.domain.evaluation.AchievementLevel

sealed interface EvidenceMark {

    data class Level(val achievementLevel: AchievementLevel) : EvidenceMark

    data object NoEvidence : EvidenceMark
}
