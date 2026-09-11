package com.emm.gema.feature.export

import com.emm.gema.core.domain.curriculum.Competency
import com.emm.gema.core.domain.curriculum.CompetencyRepository
import com.emm.gema.core.domain.curriculum.WorkedCompetencyRepository
import com.emm.gema.core.domain.evaluation.PeriodLevel
import com.emm.gema.core.domain.evaluation.PeriodLevelKey
import com.emm.gema.core.domain.evaluation.PeriodLevelRepository
import com.emm.gema.core.domain.export.ExportedFile
import com.emm.gema.core.domain.export.SiagieExportStore
import com.emm.gema.core.domain.schoolyear.Period
import com.emm.gema.core.domain.schoolyear.PeriodRepository
import com.emm.gema.core.domain.schoolyear.SchoolYear
import com.emm.gema.core.domain.schoolyear.SchoolYearRepository
import com.emm.gema.core.domain.section.Area
import com.emm.gema.core.domain.section.Section
import com.emm.gema.core.domain.section.SectionAreaRepository
import com.emm.gema.core.domain.section.SectionRepository
import com.emm.gema.core.domain.siagie.ImportedTemplate
import com.emm.gema.core.domain.siagie.ImportedTemplateKind
import com.emm.gema.core.domain.siagie.SiagieGradeEntry
import com.emm.gema.core.domain.siagie.SiagieGradesWriter
import com.emm.gema.core.domain.siagie.SiagieImportStore
import com.emm.gema.core.domain.student.Student
import com.emm.gema.core.domain.student.StudentCode
import com.emm.gema.core.domain.student.StudentRepository
import com.emm.gema.core.domain.student.orderedByName
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeSectionRepository(private val section: Section) : SectionRepository {

    override fun observeBySchoolYear(schoolYearId: String): Flow<List<Section>> =
        MutableStateFlow(listOf(section))

    override fun observeCountsBySchoolYear(): Flow<Map<String, Int>> = MutableStateFlow(emptyMap())

    override suspend fun findById(id: String): Section? = section.takeIf { it.id == id }

    override suspend fun save(section: Section) = Unit

    override suspend fun delete(id: String) = Unit
}

class FakeSchoolYearRepository(private val schoolYear: SchoolYear) : SchoolYearRepository {

    override fun observeAll(): Flow<List<SchoolYear>> = MutableStateFlow(listOf(schoolYear))

    override suspend fun findById(id: String): SchoolYear? = schoolYear.takeIf { it.id == id }

    override suspend fun save(schoolYear: SchoolYear) = Unit
}

class FakePeriodRepository(private val periods: List<Period>) : PeriodRepository {

    override fun observeBySchoolYear(schoolYearId: String): Flow<List<Period>> = MutableStateFlow(periods)

    override suspend fun findBySchoolYear(schoolYearId: String): List<Period> = periods

    override suspend fun findById(id: String): Period? = periods.find { it.id == id }

    override suspend fun saveAll(periods: List<Period>) = Unit
}

class FakeStudentRepository : StudentRepository {

    private val students: MutableStateFlow<List<Student>> = MutableStateFlow(emptyList())

    override fun observeBySection(sectionId: String): Flow<List<Student>> = students
        .map { stored -> stored.filter { it.sectionId == sectionId }.orderedByName() }

    override fun observeCountsBySection(): Flow<Map<String, Int>> = MutableStateFlow(emptyMap())

    override suspend fun listBySection(sectionId: String): List<Student> = students.value

    override suspend fun findById(id: String): Student? = students.value.find { it.id == id }

    override suspend fun findByCode(sectionId: String, code: StudentCode): Student? = students.value
        .find { it.sectionId == sectionId && it.code == code }

    override suspend fun save(student: Student) {
        students.value = students.value.filterNot { it.id == student.id } + student
    }

    override suspend fun deleteBySection(sectionId: String) = Unit
}

class FakeCompetencyRepository(private val competencies: List<Competency>) : CompetencyRepository {

    override suspend fun seed(competencies: List<Competency>, curriculumVersion: Int) = Unit

    override suspend fun findByArea(area: Area): List<Competency> = competencies
        .filter { it.area == area }
        .sortedBy { it.siagieOrdinal }
}

class FakeWorkedCompetencyRepository : WorkedCompetencyRepository {

    private val worked: MutableStateFlow<Set<String>> = MutableStateFlow(emptySet())

    override fun observeWorked(sectionId: String, periodId: String): Flow<Set<String>> = worked

    override suspend fun setWorked(
        sectionId: String,
        periodId: String,
        competencyId: String,
        isWorked: Boolean,
    ) {
        worked.value = if (isWorked) worked.value + competencyId else worked.value - competencyId
    }

    override suspend fun clearSection(sectionId: String) = Unit
}

class FakePeriodLevelRepository : PeriodLevelRepository {

    private val levels: MutableStateFlow<List<PeriodLevel>> = MutableStateFlow(emptyList())

    override fun observeByPeriod(sectionId: String, periodId: String): Flow<List<PeriodLevel>> = levels
        .map { stored -> stored.filter { it.key.periodId == periodId } }

    override fun observeRecordedCountsByPeriod(sectionId: String, periodId: String): Flow<Map<String, Int>> =
        MutableStateFlow(emptyMap())

    override fun observeRecordedCountsBySection(sectionId: String): Flow<Map<String, Int>> =
        MutableStateFlow(emptyMap())

    override suspend fun find(key: PeriodLevelKey): PeriodLevel? = levels.value.find { it.key == key }

    override suspend fun save(periodLevel: PeriodLevel) {
        levels.value = levels.value.filterNot { it.key == periodLevel.key } + periodLevel
    }

    override suspend fun delete(key: PeriodLevelKey) {
        levels.value = levels.value.filterNot { it.key == key }
    }

    override suspend fun clearSection(sectionId: String) = Unit
}

class FakeSectionAreaRepository : SectionAreaRepository {

    private val hidden: MutableStateFlow<Set<Area>> = MutableStateFlow(emptySet())

    override fun observeHiddenAreas(sectionId: String): Flow<Set<Area>> = hidden

    override suspend fun setAreaHidden(sectionId: String, area: Area, isHidden: Boolean) {
        hidden.value = if (isHidden) hidden.value + area else hidden.value - area
    }

    override suspend fun clearSection(sectionId: String) = Unit
}

class FakeSiagieImportStore : SiagieImportStore {

    private val templates: MutableMap<String, ImportedTemplate> = mutableMapOf()

    override suspend fun apply(students: List<Student>, template: ImportedTemplate) {
        templates["${template.sectionId}/${template.kind}"] = template
    }

    override suspend fun clearSection(sectionId: String) = Unit

    override suspend fun findTemplate(sectionId: String, kind: ImportedTemplateKind): ImportedTemplate? =
        templates["$sectionId/$kind"]
}

class FakeGradesWriter : SiagieGradesWriter {

    override fun write(template: ByteArray, entries: List<SiagieGradeEntry>): ByteArray = template
}

class FakeExportStore : SiagieExportStore {

    override suspend fun write(fileName: String, content: ByteArray): ExportedFile =
        ExportedFile(name = fileName, path = "/cache/exports/$fileName")
}
