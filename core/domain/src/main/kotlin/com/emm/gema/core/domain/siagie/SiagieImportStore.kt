package com.emm.gema.core.domain.siagie

import com.emm.gema.core.domain.student.Student

interface SiagieImportStore {

    suspend fun apply(students: List<Student>, template: ImportedTemplate)

    suspend fun clearSection(sectionId: String)

    suspend fun findTemplate(sectionId: String, kind: ImportedTemplateKind): ImportedTemplate?
}
