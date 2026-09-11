package com.emm.gema.core.domain.siagie

import com.emm.gema.core.domain.student.StudentCode

data class SiagieImportPlan(
    val fileName: String,
    val rosterSize: Int,
    val created: List<SiagieImportEntry>,
    val updated: List<SiagieImportEntry>,
    val missing: List<SiagieImportMissing>,
)

data class SiagieImportEntry(
    val studentId: String?,
    val code: StudentCode,
    val fullName: String,
    val siagieId: String?,
)

data class SiagieImportMissing(
    val studentId: String,
    val fullName: String,
)
