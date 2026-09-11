package com.emm.gema.feature.students

import com.emm.gema.core.domain.siagie.ImportedTemplate
import com.emm.gema.core.domain.siagie.ImportedTemplateKind
import com.emm.gema.core.domain.siagie.SiagieDocuments
import com.emm.gema.core.domain.siagie.SiagieImportStore
import com.emm.gema.core.domain.siagie.SiagieRoster
import com.emm.gema.core.domain.siagie.SiagieRosterReader
import com.emm.gema.core.domain.siagie.SiagieRosterResult
import com.emm.gema.core.domain.student.Student

class FakeSiagieDocuments(private val fileName: String) : SiagieDocuments {

    override suspend fun nameOf(uri: String): String = fileName

    override suspend fun readContent(uri: String): ByteArray = byteArrayOf(1, 2, 3)
}

class FakeSiagieRosterReader : SiagieRosterReader {

    var roster: SiagieRoster? = null

    override fun read(fileName: String, content: ByteArray): SiagieRosterResult {
        val parsed: SiagieRoster = roster ?: return SiagieRosterResult.NotASiagieTemplate
        return SiagieRosterResult.Parsed(parsed)
    }
}

class FakeSiagieImportStore(private val students: FakeStudentRepository) : SiagieImportStore {

    private val templates: MutableMap<String, ImportedTemplate> = mutableMapOf()

    override suspend fun apply(students: List<Student>, template: ImportedTemplate) {
        students.forEach { this.students.save(it) }
        templates[template.sectionId] = template
    }

    override suspend fun clearSection(sectionId: String) {
        templates.remove(sectionId)
    }

    override suspend fun findTemplate(sectionId: String, kind: ImportedTemplateKind): ImportedTemplate? =
        templates[sectionId]?.takeIf { it.kind == kind }
}
