package com.emm.gema.core.database.evaluation

import com.emm.gema.core.database.Period_level as PeriodLevelRow
import com.emm.gema.core.domain.evaluation.AchievementLevel
import com.emm.gema.core.domain.evaluation.PeriodLevel
import com.emm.gema.core.domain.evaluation.PeriodLevelKey
import com.emm.gema.core.domain.evaluation.UnworkedComment

fun PeriodLevelRow.toDomain(): PeriodLevel = PeriodLevel(
    key = PeriodLevelKey(
        sectionId = section_id,
        periodId = period_id,
        studentId = student_id,
        competencyId = competency_id,
    ),
    achievementLevel = achievement_level?.let(AchievementLevel::valueOf),
    unworkedComment = unworked_comment?.let(UnworkedComment::valueOf),
    descriptiveConclusion = descriptive_conclusion,
)
