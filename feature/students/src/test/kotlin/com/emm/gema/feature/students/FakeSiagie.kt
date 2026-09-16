package com.emm.gema.feature.students

import com.emm.gema.core.domain.section.SectionId
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
    var malformedRow: Int? = null

    override fun read(fileName: String, content: ByteArray): SiagieRosterResult {
        malformedRow?.let { row: Int -> return SiagieRosterResult.Malformed(row) }
        val parsed: SiagieRoster = roster ?: return SiagieRosterResult.NotASiagieTemplate
        return SiagieRosterResult.Parsed(parsed)
    }
}

class FakeSiagieImportStore(private val students: FakeStudentRepository) : SiagieImportStore {

    private val templates: MutableMap<SectionId, ImportedTemplate> = mutableMapOf()

    override suspend fun apply(students: List<Student>, template: ImportedTemplate) {
        students.forEach { this.students.save(it) }
        templates[template.sectionId] = template
    }

    override suspend fun clearSection(sectionId: SectionId) {
        templates.remove(sectionId)
    }

    override suspend fun findTemplate(sectionId: SectionId, kind: ImportedTemplateKind): ImportedTemplate? =
        templates[sectionId]?.takeIf { it.kind == kind }
}
