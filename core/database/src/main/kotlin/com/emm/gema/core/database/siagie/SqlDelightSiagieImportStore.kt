package com.emm.gema.core.database.siagie

import com.emm.gema.core.database.GemaDb
import com.emm.gema.core.database.ImportedTemplateQueries
import com.emm.gema.core.database.StudentQueries
import com.emm.gema.core.domain.siagie.ImportedTemplate
import com.emm.gema.core.domain.siagie.ImportedTemplateKind
import com.emm.gema.core.domain.siagie.SiagieImportStore
import com.emm.gema.core.domain.student.Student
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SqlDelightSiagieImportStore(
    private val database: GemaDb,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : SiagieImportStore {

    private val templates: ImportedTemplateQueries = database.importedTemplateQueries
    private val studentQueries: StudentQueries = database.studentQueries

    override suspend fun apply(students: List<Student>, template: ImportedTemplate): Unit =
        withContext(dispatcher) {
            database.transaction {
                students.forEach(::insert)
                templates.insert(
                    section_id = template.sectionId,
                    kind = template.kind.name,
                    file_name = template.fileName,
                    content = template.content,
                    imported_at = template.importedAt.toString(),
                )
            }
        }

    override suspend fun clearSection(sectionId: String): Unit = withContext(dispatcher) {
        templates.deleteBySection(sectionId)
    }

    override suspend fun findTemplate(sectionId: String, kind: ImportedTemplateKind): ImportedTemplate? =
        withContext(dispatcher) {
            templates.selectBySectionAndKind(sectionId, kind.name).executeAsOneOrNull()?.toDomain()
        }

    private fun insert(student: Student) {
        studentQueries.insert(
            id = student.id,
            section_id = student.sectionId,
            student_code = student.code.value,
            full_name = student.fullName,
            siagie_id = student.siagieId,
            withdrawal_date = student.withdrawalDate?.toString(),
        )
    }
}
