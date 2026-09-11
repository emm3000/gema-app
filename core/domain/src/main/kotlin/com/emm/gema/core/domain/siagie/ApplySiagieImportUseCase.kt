package com.emm.gema.core.domain.siagie

import com.emm.gema.core.domain.id.IdGenerator
import com.emm.gema.core.domain.student.Student
import com.emm.gema.core.domain.student.StudentRepository
import java.time.Clock
import java.time.LocalDate

class ApplySiagieImportUseCase(
    private val planner: SiagieImportPlanner,
    private val students: StudentRepository,
    private val store: SiagieImportStore,
    private val idGenerator: IdGenerator,
    private val clock: Clock,
) {

    suspend operator fun invoke(
        sectionId: String,
        uri: String,
        withdrawals: Set<String>,
        withdrawalDate: LocalDate,
    ): SiagieImportResult {
        val planned: PlannedImport = planner.plan(sectionId, uri)
        if (planned is PlannedImport.Rejected) return SiagieImportResult.Rejected(planned.reason)
        val ready: PlannedImport.Ready = planned as PlannedImport.Ready
        val created: List<Student> = ready.plan.created.map { it.asNewStudent(sectionId) }
        val updated: List<Student> = ready.plan.updated.mapNotNull { it.asMergedStudent() }
        val withdrawn: List<Student> = ready.plan.missing
            .filter { it.studentId in withdrawals }
            .mapNotNull { students.findById(it.studentId)?.copy(withdrawalDate = withdrawalDate) }

        store.apply(created + updated + withdrawn, templateOf(sectionId, ready))
        return SiagieImportResult.Applied(
            created = created.size,
            updated = updated.size,
            withdrawn = withdrawn.size,
        )
    }

    private fun templateOf(sectionId: String, ready: PlannedImport.Ready): ImportedTemplate = ImportedTemplate(
        sectionId = sectionId,
        kind = ImportedTemplateKind.GRADES,
        fileName = ready.plan.fileName,
        content = ready.content,
        importedAt = clock.instant(),
    )

    private fun SiagieImportEntry.asNewStudent(sectionId: String): Student = Student(
        id = idGenerator.newId(),
        sectionId = sectionId,
        code = code,
        fullName = fullName,
        siagieId = siagieId,
    )

    private suspend fun SiagieImportEntry.asMergedStudent(): Student? {
        val enrolled: Student = students.findById(requireNotNull(studentId)) ?: return null
        return enrolled.copy(
            fullName = fullName,
            siagieId = siagieId ?: enrolled.siagieId,
            withdrawalDate = null,
        )
    }
}
