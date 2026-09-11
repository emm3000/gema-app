package com.emm.gema.feature.evaluation.levels

import com.emm.gema.core.domain.evaluation.AchievementLevel
import com.emm.gema.core.ui.GLevelOption

fun AchievementLevel.toOption(): GLevelOption = when (this) {
    AchievementLevel.AD -> GLevelOption.AD
    AchievementLevel.A -> GLevelOption.A
    AchievementLevel.B -> GLevelOption.B
    AchievementLevel.C -> GLevelOption.C
}

fun GLevelOption.toAchievementLevel(): AchievementLevel = when (this) {
    GLevelOption.AD -> AchievementLevel.AD
    GLevelOption.A -> AchievementLevel.A
    GLevelOption.B -> AchievementLevel.B
    GLevelOption.C -> AchievementLevel.C
}
