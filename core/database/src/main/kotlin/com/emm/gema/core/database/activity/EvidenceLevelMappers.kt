package com.emm.gema.core.database.activity

import com.emm.gema.core.database.Evidence_level as EvidenceLevelRow
import com.emm.gema.core.database.SelectForStudentAndCompetency
import com.emm.gema.core.domain.activity.EvidenceLevel
import com.emm.gema.core.domain.activity.EvidenceLevelKey
import com.emm.gema.core.domain.activity.EvidenceRecord
import com.emm.gema.core.domain.evaluation.AchievementLevel
import java.time.LocalDate

fun EvidenceLevelRow.toDomain(): EvidenceLevel = EvidenceLevel(
    key = EvidenceLevelKey(
        activityId = activity_id,
        studentId = student_id,
        competencyId = competency_id,
    ),
    achievementLevel = AchievementLevel.valueOf(achievement_level),
)

fun SelectForStudentAndCompetency.toDomain(): EvidenceRecord = EvidenceRecord(
    activityId = activity_id,
    activityName = activity_name,
    date = LocalDate.parse(date),
    achievementLevel = AchievementLevel.valueOf(achievement_level),
)
