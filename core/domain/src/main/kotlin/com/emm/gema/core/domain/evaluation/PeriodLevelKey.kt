package com.emm.gema.core.domain.evaluation

import com.emm.gema.core.domain.curriculum.CompetencyId
import com.emm.gema.core.domain.schoolyear.PeriodId
import com.emm.gema.core.domain.section.SectionId
import com.emm.gema.core.domain.student.StudentId

data class PeriodLevelKey(
    val sectionId: SectionId,
    val periodId: PeriodId,
    val studentId: StudentId,
    val competencyId: CompetencyId,
)
