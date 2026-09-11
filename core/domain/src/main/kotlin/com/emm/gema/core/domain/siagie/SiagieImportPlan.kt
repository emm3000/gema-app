package com.emm.gema.core.domain.siagie

import com.emm.gema.core.domain.student.StudentCode
import com.emm.gema.core.domain.student.StudentId

data class SiagieImportPlan(
    val fileName: String,
    val rosterSize: Int,
    val created: List<SiagieImportEntry>,
    val updated: List<SiagieImportEntry>,
    val missing: List<SiagieImportMissing>,
)

data class SiagieImportEntry(
    val studentId: StudentId?,
    val code: StudentCode,
    val fullName: String,
    val siagieId: String?,
)

data class SiagieImportMissing(
    val studentId: StudentId,
    val fullName: String,
)
