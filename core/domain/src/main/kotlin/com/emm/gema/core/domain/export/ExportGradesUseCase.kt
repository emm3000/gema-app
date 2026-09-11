package com.emm.gema.core.domain.export

import com.emm.gema.core.domain.siagie.ImportedTemplate
import com.emm.gema.core.domain.siagie.ImportedTemplateKind
import com.emm.gema.core.domain.siagie.SiagieGradesWriter
import com.emm.gema.core.domain.siagie.SiagieImportStore
import kotlinx.coroutines.flow.first

class ExportGradesUseCase(
    private val getPlan: GetGradesExportPlanUseCase,
    private val importStore: SiagieImportStore,
    private val writer: SiagieGradesWriter,
    private val exportStore: SiagieExportStore,
) {

    suspend operator fun invoke(sectionId: String, periodId: String): GradesExportResult {
        val template: ImportedTemplate = importStore.findTemplate(sectionId, ImportedTemplateKind.GRADES)
            ?: return GradesExportResult.Unavailable
        val plan: GradesExportPlan = getPlan(sectionId, periodId).first()
        if (!plan.isReady) return GradesExportResult.Blocked(plan.gaps)

        val filled: ByteArray = writer.write(template.content, plan.entries)
        return GradesExportResult.Exported(exportStore.write(template.fileName, filled))
    }
}
