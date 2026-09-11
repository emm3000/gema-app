package com.emm.gema.core.domain.activity

import com.emm.gema.core.domain.curriculum.CompetencyId
import com.emm.gema.core.domain.student.StudentId

data class EvidenceLevelKey(
    val activityId: ActivityId,
    val studentId: StudentId,
    val competencyId: CompetencyId,
)
