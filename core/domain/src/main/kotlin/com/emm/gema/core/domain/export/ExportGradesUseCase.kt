package com.emm.gema.core.domain.export

import com.emm.gema.core.domain.schoolyear.PeriodId
import com.emm.gema.core.domain.section.SectionId
import com.emm.gema.core.domain.siagie.ImportedTemplate
import com.emm.gema.core.domain.siagie.ImportedTemplateKind
import com.emm.gema.core.domain.siagie.SiagieGradesWriteResult
import com.emm.gema.core.domain.siagie.SiagieGradesWriter
import com.emm.gema.core.domain.siagie.SiagieImportStore
import com.emm.gema.core.domain.student.StudentCode
import com.emm.gema.core.domain.student.StudentRepository
import kotlinx.coroutines.flow.first

class ExportGradesUseCase(
    private val getPlan: GetGradesExportPlanUseCase,
    private val importStore: SiagieImportStore,
    private val writer: SiagieGradesWriter,
    private val exportStore: SiagieExportStore,
    private val students: StudentRepository,
) {

    suspend operator fun invoke(sectionId: SectionId, periodId: PeriodId): GradesExportResult {
        val template: ImportedTemplate = importStore.findTemplate(sectionId, ImportedTemplateKind.GRADES)
            ?: return GradesExportResult.Unavailable
        val plan: GradesExportPlan = getPlan(sectionId, periodId).first()
        if (!plan.isReady) return GradesExportResult.Blocked(plan.gaps)

        return when (val written: SiagieGradesWriteResult = writer.write(template.content, plan.entries)) {
            is SiagieGradesWriteResult.Unmapped -> mismatchOf(sectionId, written)
            is SiagieGradesWriteResult.Written ->
                GradesExportResult.Exported(exportStore.write(template.fileName, written.content))
        }
    }

    private suspend fun mismatchOf(
        sectionId: SectionId,
        unmapped: SiagieGradesWriteResult.Unmapped,
    ): GradesExportResult.TemplateMismatch = GradesExportResult.TemplateMismatch(
        areas = unmapped.areas,
        studentNames = unmapped.studentCodes.map { code: StudentCode ->
            students.findByCode(sectionId, code)?.fullName ?: code.value
        },
        competencies = unmapped.competencies,
    )
}
