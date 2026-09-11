package com.emm.gema.core.siagie

import com.emm.gema.core.domain.id.IdGenerator
import com.emm.gema.core.domain.schoolyear.SchoolYearId
import com.emm.gema.core.domain.section.Section
import com.emm.gema.core.domain.section.SectionId
import com.emm.gema.core.domain.section.SectionRepository
import com.emm.gema.core.domain.siagie.ImportedTemplate
import com.emm.gema.core.domain.siagie.ImportedTemplateKind
import com.emm.gema.core.domain.siagie.SiagieDocuments
import com.emm.gema.core.domain.siagie.SiagieImportStore
import com.emm.gema.core.domain.student.Student
import com.emm.gema.core.domain.student.StudentCode
import com.emm.gema.core.domain.student.StudentId
import com.emm.gema.core.domain.student.StudentRepository
import com.emm.gema.core.domain.student.orderedByName
import java.io.File
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeSectionRepository : SectionRepository {

    var section: Section? = null

    override fun observeBySchoolYear(schoolYearId: SchoolYearId): Flow<List<Section>> =
        MutableStateFlow(listOfNotNull(section))

    override fun observeCountsBySchoolYear(): Flow<Map<SchoolYearId, Int>> = MutableStateFlow(emptyMap())

    override suspend fun findById(id: SectionId): Section? = section?.takeIf { it.id == id }

    override suspend fun save(section: Section) {
        this.section = section
    }

    override suspend fun delete(id: SectionId) {
        section = null
    }
}

class FakeStudentRepository : StudentRepository {

    private val students: MutableStateFlow<List<Student>> = MutableStateFlow(emptyList())

    override fun observeBySection(sectionId: SectionId): Flow<List<Student>> = students
        .map { stored -> stored.filter { it.sectionId == sectionId }.orderedByName() }

    override fun observeCountsBySection(): Flow<Map<SectionId, Int>> = students
        .map { stored -> stored.groupingBy { it.sectionId }.eachCount() }

    override suspend fun listBySection(sectionId: SectionId): List<Student> = students.value
        .filter { it.sectionId == sectionId }
        .orderedByName()

    override suspend fun findById(id: StudentId): Student? = students.value.find { it.id == id }

    override suspend fun findByCode(sectionId: SectionId, code: StudentCode): Student? = students.value
        .find { it.sectionId == sectionId && it.code == code }

    override suspend fun save(student: Student) {
        students.value = students.value.filterNot { it.id == student.id } + student
    }

    override suspend fun deleteBySection(sectionId: SectionId) {
        students.value = students.value.filterNot { it.sectionId == sectionId }
    }
}

class FakeSiagieImportStore(private val students: FakeStudentRepository) : SiagieImportStore {

    private val templates: MutableMap<String, ImportedTemplate> = mutableMapOf()

    override suspend fun apply(students: List<Student>, template: ImportedTemplate) {
        students.forEach { this.students.save(it) }
        templates["${template.sectionId.value}/${template.kind}"] = template
    }

    override suspend fun clearSection(sectionId: SectionId) {
        templates.keys.filter { it.startsWith("${sectionId.value}/") }.forEach(templates::remove)
    }

    override suspend fun findTemplate(sectionId: SectionId, kind: ImportedTemplateKind): ImportedTemplate? =
        templates["${sectionId.value}/$kind"]
}

class FileSiagieDocuments : SiagieDocuments {

    override suspend fun nameOf(uri: String): String = File(uri).name

    override suspend fun readContent(uri: String): ByteArray = File(uri).readBytes()
}

class SequentialIdGenerator : IdGenerator {

    private var next: Int = 0

    override fun newId(): String {
        next++
        return "student-$next"
    }
}
