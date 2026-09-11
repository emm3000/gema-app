package com.emm.gema.core.domain.export

import com.emm.gema.core.domain.curriculum.Competency
import com.emm.gema.core.domain.student.StudentId

data class ExportGap(
    val studentId: StudentId,
    val studentName: String,
    val competency: Competency,
)
