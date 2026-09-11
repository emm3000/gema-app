package com.emm.gema.core.domain.export

sealed interface GradesExportResult {

    data object Unavailable : GradesExportResult

    data class Blocked(val gaps: List<ExportGap>) : GradesExportResult

    data class Exported(val file: ExportedFile) : GradesExportResult
}
