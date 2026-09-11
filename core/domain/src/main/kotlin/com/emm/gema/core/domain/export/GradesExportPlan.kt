package com.emm.gema.core.domain.export

import com.emm.gema.core.domain.siagie.SiagieGradeEntry

data class GradesExportPlan(
    val gaps: List<ExportGap> = emptyList(),
    val entries: List<SiagieGradeEntry> = emptyList(),
) {
    val isReady: Boolean get() = gaps.isEmpty()
}
