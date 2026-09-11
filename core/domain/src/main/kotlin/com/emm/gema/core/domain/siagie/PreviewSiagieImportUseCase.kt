package com.emm.gema.core.domain.siagie

class PreviewSiagieImportUseCase(
    private val planner: SiagieImportPlanner,
) {

    suspend operator fun invoke(sectionId: String, uri: String): SiagieImportPreview =
        when (val planned: PlannedImport = planner.plan(sectionId, uri)) {
            is PlannedImport.Ready -> SiagieImportPreview.Ready(planned.plan)
            is PlannedImport.Rejected -> SiagieImportPreview.Rejected(planned.reason)
        }
}
