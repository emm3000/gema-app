package com.emm.gema.core.domain.fake

import com.emm.gema.core.domain.siagie.ImportedTemplate
import com.emm.gema.core.domain.siagie.ImportedTemplateKind
import com.emm.gema.core.domain.siagie.SiagieImportStore
import com.emm.gema.core.domain.student.Student

class InMemorySiagieImportStore(
    private val students: InMemoryStudentRepository,
) : SiagieImportStore {

    private val templates: MutableMap<String, ImportedTemplate> = mutableMapOf()

    override suspend fun apply(students: List<Student>, template: ImportedTemplate) {
        students.forEach { this.students.save(it) }
        templates[key(template.sectionId, template.kind)] = template
    }

    override suspend fun findTemplate(sectionId: String, kind: ImportedTemplateKind): ImportedTemplate? =
        templates[key(sectionId, kind)]

    private fun key(sectionId: String, kind: ImportedTemplateKind): String = "$sectionId/$kind"
}
