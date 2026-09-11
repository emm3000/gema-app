package com.emm.gema.core.domain.export

import com.emm.gema.core.domain.curriculum.Competency

data class ExportGap(
    val studentId: String,
    val studentName: String,
    val competency: Competency,
)
