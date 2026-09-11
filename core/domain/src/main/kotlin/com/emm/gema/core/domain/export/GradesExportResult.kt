package com.emm.gema.core.domain.export

import com.emm.gema.core.domain.section.Area

sealed interface GradesExportResult {

    data object Unavailable : GradesExportResult

    data class Blocked(val gaps: List<ExportGap>) : GradesExportResult

    data class TemplateMismatch(
        val areas: List<Area>,
        val studentNames: List<String>,
    ) : GradesExportResult

    data class Exported(val file: ExportedFile) : GradesExportResult
}
