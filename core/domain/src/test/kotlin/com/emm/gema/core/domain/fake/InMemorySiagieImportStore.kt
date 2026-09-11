package com.emm.gema.core.domain.fake

import com.emm.gema.core.domain.section.SectionId
import com.emm.gema.core.domain.siagie.ImportedTemplate
import com.emm.gema.core.domain.siagie.ImportedTemplateKind
import com.emm.gema.core.domain.siagie.SiagieImportStore
import com.emm.gema.core.domain.student.Student
import com.emm.gema.core.domain.student.StudentRepository

class InMemorySiagieImportStore(
    private val students: StudentRepository,
) : SiagieImportStore {

    private val templates: MutableMap<String, ImportedTemplate> = mutableMapOf()

    override suspend fun apply(students: List<Student>, template: ImportedTemplate) {
        students.forEach { this.students.save(it) }
        templates[key(template.sectionId, template.kind)] = template
    }

    override suspend fun clearSection(sectionId: SectionId) {
        templates.keys.filter { it.startsWith("${sectionId.value}/") }.forEach(templates::remove)
    }

    override suspend fun findTemplate(sectionId: SectionId, kind: ImportedTemplateKind): ImportedTemplate? =
        templates[key(sectionId, kind)]

    private fun key(sectionId: SectionId, kind: ImportedTemplateKind): String = "${sectionId.value}/$kind"
}
