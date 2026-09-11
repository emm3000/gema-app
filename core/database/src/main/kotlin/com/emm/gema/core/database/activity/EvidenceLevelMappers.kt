package com.emm.gema.core.database.activity

import com.emm.gema.core.database.Evidence_level as EvidenceLevelRow
import com.emm.gema.core.database.SelectForStudentAndCompetency
import com.emm.gema.core.domain.activity.ActivityId
import com.emm.gema.core.domain.activity.EvidenceLevel
import com.emm.gema.core.domain.activity.EvidenceLevelKey
import com.emm.gema.core.domain.activity.EvidenceRecord
import com.emm.gema.core.domain.curriculum.CompetencyId
import com.emm.gema.core.domain.evaluation.AchievementLevel
import com.emm.gema.core.domain.student.StudentId
import java.time.LocalDate

fun EvidenceLevelRow.toDomain(): EvidenceLevel = EvidenceLevel(
    key = EvidenceLevelKey(
        activityId = ActivityId(activity_id),
        studentId = StudentId(student_id),
        competencyId = CompetencyId(competency_id),
    ),
    achievementLevel = achievement_level?.let(AchievementLevel::valueOf),
)

fun SelectForStudentAndCompetency.toDomain(): EvidenceRecord = EvidenceRecord(
    activityId = ActivityId(activity_id),
    activityName = activity_name,
    date = LocalDate.parse(date),
    achievementLevel = AchievementLevel.valueOf(requireNotNull(achievement_level)),
)
