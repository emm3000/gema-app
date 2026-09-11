package com.emm.gema.core.domain.siagie

sealed interface SiagieImportResult {

    data class Applied(val created: Int, val updated: Int, val withdrawn: Int) : SiagieImportResult

    data class Rejected(val reason: SiagieImportRejection) : SiagieImportResult
}
