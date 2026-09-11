package com.emm.gema.core.database.evaluation

import com.emm.gema.core.database.Period_level as PeriodLevelRow
import com.emm.gema.core.domain.curriculum.CompetencyId
import com.emm.gema.core.domain.evaluation.AchievementLevel
import com.emm.gema.core.domain.evaluation.PeriodLevel
import com.emm.gema.core.domain.evaluation.PeriodLevelKey
import com.emm.gema.core.domain.evaluation.UnworkedComment
import com.emm.gema.core.domain.schoolyear.PeriodId
import com.emm.gema.core.domain.section.SectionId
import com.emm.gema.core.domain.student.StudentId

fun PeriodLevelRow.toDomain(): PeriodLevel = PeriodLevel(
    key = PeriodLevelKey(
        sectionId = SectionId(section_id),
        periodId = PeriodId(period_id),
        studentId = StudentId(student_id),
        competencyId = CompetencyId(competency_id),
    ),
    achievementLevel = achievement_level?.let(AchievementLevel::valueOf),
    unworkedComment = unworked_comment?.let(UnworkedComment::valueOf),
    descriptiveConclusion = descriptive_conclusion,
)
