package com.emm.gema.core.domain.siagie

import com.emm.gema.core.domain.section.SectionId
import com.emm.gema.core.domain.student.Student

interface SiagieImportStore {

    suspend fun apply(students: List<Student>, template: ImportedTemplate)

    suspend fun clearSection(sectionId: SectionId)

    suspend fun findTemplate(sectionId: SectionId, kind: ImportedTemplateKind): ImportedTemplate?
}
