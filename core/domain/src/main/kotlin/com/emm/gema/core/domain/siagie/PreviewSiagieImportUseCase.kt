package com.emm.gema.core.domain.siagie

import com.emm.gema.core.domain.section.SectionId

class PreviewSiagieImportUseCase(
    private val planner: SiagieImportPlanner,
) {

    suspend operator fun invoke(sectionId: SectionId, uri: String): SiagieImportPreview =
        when (val planned: PlannedImport = planner.plan(sectionId, uri)) {
            is PlannedImport.Ready -> SiagieImportPreview.Ready(planned.plan)
            is PlannedImport.Rejected -> SiagieImportPreview.Rejected(planned.reason, planned.fileName)
        }
}
