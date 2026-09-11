package com.emm.gema.core.domain.siagie

sealed interface SiagieImportPreview {

    data class Ready(val plan: SiagieImportPlan) : SiagieImportPreview

    data class Rejected(val reason: SiagieImportRejection) : SiagieImportPreview
}

sealed interface SiagieImportRejection {

    data object NotASiagieTemplate : SiagieImportRejection

    data object EmptyRoster : SiagieImportRejection

    data class MalformedRow(val row: Int) : SiagieImportRejection

    data class GradeMismatch(val expected: Int, val found: Int) : SiagieImportRejection

    data class SectionMismatch(val expected: String, val found: String) : SiagieImportRejection
}
