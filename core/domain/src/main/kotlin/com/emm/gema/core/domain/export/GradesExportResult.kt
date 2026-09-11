package com.emm.gema.core.domain.export

import com.emm.gema.core.domain.section.Area
import com.emm.gema.core.domain.siagie.SiagieCompetencyColumn

sealed interface GradesExportResult {

    data object Unavailable : GradesExportResult

    data class Blocked(val gaps: List<ExportGap>) : GradesExportResult

    data class TemplateMismatch(
        val areas: List<Area>,
        val studentNames: List<String>,
        val competencies: List<SiagieCompetencyColumn>,
    ) : GradesExportResult

    data class Exported(val file: ExportedFile) : GradesExportResult
}
