package com.emm.gema.feature.export

import com.emm.gema.core.domain.attendance.AttendanceRecord
import com.emm.gema.core.domain.attendance.AttendanceRepository
import com.emm.gema.core.domain.curriculum.Competency
import com.emm.gema.core.domain.curriculum.CompetencyId
import com.emm.gema.core.domain.curriculum.CompetencyRepository
import com.emm.gema.core.domain.curriculum.WorkedCompetencyRepository
import com.emm.gema.core.domain.evaluation.PeriodLevel
import com.emm.gema.core.domain.evaluation.PeriodLevelKey
import com.emm.gema.core.domain.evaluation.PeriodLevelRepository
import com.emm.gema.core.domain.evaluation.PeriodLevelSummary
import com.emm.gema.core.domain.evaluation.PeriodLevelSummaryPdfRenderer
import com.emm.gema.core.domain.evaluation.SummaryDocuments
import com.emm.gema.core.domain.evaluation.SummaryFile
import com.emm.gema.core.domain.export.ExportedFile
import com.emm.gema.core.domain.export.SiagieExportStore
import com.emm.gema.core.domain.schoolyear.Period
import com.emm.gema.core.domain.schoolyear.PeriodId
import com.emm.gema.core.domain.schoolyear.PeriodRepository
import com.emm.gema.core.domain.schoolyear.SchoolYear
import com.emm.gema.core.domain.schoolyear.SchoolYearId
import com.emm.gema.core.domain.schoolyear.SchoolYearRepository
import com.emm.gema.core.domain.section.Area
import com.emm.gema.core.domain.section.Section
import com.emm.gema.core.domain.section.SectionAreaRepository
import com.emm.gema.core.domain.section.SectionId
import com.emm.gema.core.domain.section.SectionRepository
import com.emm.gema.core.domain.siagie.AttendanceExportEntry
import com.emm.gema.core.domain.siagie.AttendanceExportFile
import com.emm.gema.core.domain.siagie.ImportedTemplate
import com.emm.gema.core.domain.siagie.ImportedTemplateKind
import com.emm.gema.core.domain.siagie.MonthlyAttendanceExporter
import com.emm.gema.core.domain.siagie.SiagieGradeEntry
import com.emm.gema.core.domain.siagie.SiagieGradesWriteResult
import com.emm.gema.core.domain.siagie.SiagieGradesWriter
import com.emm.gema.core.domain.siagie.SiagieImportStore
import com.emm.gema.core.domain.student.Student
import com.emm.gema.core.domain.student.StudentCode
import com.emm.gema.core.domain.student.StudentId
import com.emm.gema.core.domain.student.StudentRepository
import com.emm.gema.core.domain.student.orderedByName
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeSectionRepository(private val section: Section) : SectionRepository {

    override fun observeBySchoolYear(schoolYearId: SchoolYearId): Flow<List<Section>> =
        MutableStateFlow(listOf(section))

    override fun observeCountsBySchoolYear(): Flow<Map<SchoolYearId, Int>> = MutableStateFlow(emptyMap())

    override suspend fun findById(id: SectionId): Section? = section.takeIf { it.id == id }

    override suspend fun save(section: Section) = Unit

    override suspend fun delete(id: SectionId) = Unit
}

class FakeSchoolYearRepository(private val schoolYear: SchoolYear) : SchoolYearRepository {

    override fun observeAll(): Flow<List<SchoolYear>> = MutableStateFlow(listOf(schoolYear))

    override suspend fun findById(id: SchoolYearId): SchoolYear? = schoolYear.takeIf { it.id == id }

    override suspend fun save(schoolYear: SchoolYear) = Unit
}

class FakePeriodRepository(private val periods: List<Period>) : PeriodRepository {

    override fun observeBySchoolYear(schoolYearId: SchoolYearId): Flow<List<Period>> = MutableStateFlow(periods)

    override suspend fun findBySchoolYear(schoolYearId: SchoolYearId): List<Period> = periods

    override suspend fun findById(id: PeriodId): Period? = periods.find { it.id == id }

    override suspend fun saveAll(periods: List<Period>) = Unit
}

class FakeStudentRepository : StudentRepository {

    private val students: MutableStateFlow<List<Student>> = MutableStateFlow(emptyList())

    override fun observeBySection(sectionId: SectionId): Flow<List<Student>> = students
        .map { stored -> stored.filter { it.sectionId == sectionId }.orderedByName() }

    override fun observeCountsBySection(): Flow<Map<SectionId, Int>> = MutableStateFlow(emptyMap())

    override suspend fun listBySection(sectionId: SectionId): List<Student> = students.value

    override suspend fun findById(id: StudentId): Student? = students.value.find { it.id == id }

    override suspend fun findByCode(sectionId: SectionId, code: StudentCode): Student? = students.value
        .find { it.sectionId == sectionId && it.code == code }

    override suspend fun save(student: Student) {
        students.value = students.value.filterNot { it.id == student.id } + student
    }

    override suspend fun deleteBySection(sectionId: SectionId) = Unit
}

class FakeCompetencyRepository(private val competencies: List<Competency>) : CompetencyRepository {

    override suspend fun seed(competencies: List<Competency>, curriculumVersion: Int) = Unit

    override suspend fun findByArea(area: Area): List<Competency> = competencies
        .filter { it.area == area }
        .sortedBy { it.siagieOrdinal }
}

class FakeWorkedCompetencyRepository : WorkedCompetencyRepository {

    private val worked: MutableStateFlow<Set<CompetencyId>> = MutableStateFlow(emptySet())

    override fun observeWorked(sectionId: SectionId, periodId: PeriodId): Flow<Set<CompetencyId>> = worked

    override suspend fun setWorked(
        sectionId: SectionId,
        periodId: PeriodId,
        competencyId: CompetencyId,
        isWorked: Boolean,
    ) {
        worked.value = if (isWorked) worked.value + competencyId else worked.value - competencyId
    }

    override suspend fun clearSection(sectionId: SectionId) = Unit
}

class FakePeriodLevelRepository : PeriodLevelRepository {

    private val levels: MutableStateFlow<List<PeriodLevel>> = MutableStateFlow(emptyList())

    override fun observeByPeriod(sectionId: SectionId, periodId: PeriodId): Flow<List<PeriodLevel>> = levels
        .map { stored -> stored.filter { it.key.periodId == periodId } }

    override fun observeRecordedCountsByPeriod(sectionId: SectionId, periodId: PeriodId): Flow<Map<CompetencyId, Int>> =
        MutableStateFlow(emptyMap())

    override fun observeRecordedCountsBySection(sectionId: SectionId): Flow<Map<CompetencyId, Int>> =
        MutableStateFlow(emptyMap())

    override suspend fun find(key: PeriodLevelKey): PeriodLevel? = levels.value.find { it.key == key }

    override suspend fun save(periodLevel: PeriodLevel) {
        levels.value = levels.value.filterNot { it.key == periodLevel.key } + periodLevel
    }

    override suspend fun delete(key: PeriodLevelKey) {
        levels.value = levels.value.filterNot { it.key == key }
    }

    override suspend fun clearSection(sectionId: SectionId) = Unit
}

class FakeSectionAreaRepository : SectionAreaRepository {

    private val hidden: MutableStateFlow<Set<Area>> = MutableStateFlow(emptySet())

    override fun observeHiddenAreas(sectionId: SectionId): Flow<Set<Area>> = hidden

    override suspend fun setAreaHidden(sectionId: SectionId, area: Area, isHidden: Boolean) {
        hidden.value = if (isHidden) hidden.value + area else hidden.value - area
    }

    override suspend fun clearSection(sectionId: SectionId) = Unit
}

class FakeSiagieImportStore : SiagieImportStore {

    private val templates: MutableMap<String, ImportedTemplate> = mutableMapOf()

    override suspend fun apply(students: List<Student>, template: ImportedTemplate) {
        templates["${template.sectionId.value}/${template.kind}"] = template
    }

    override suspend fun clearSection(sectionId: SectionId) = Unit

    override suspend fun findTemplate(sectionId: SectionId, kind: ImportedTemplateKind): ImportedTemplate? =
        templates["${sectionId.value}/$kind"]
}

class FakeGradesWriter : SiagieGradesWriter {

    var unmapped: SiagieGradesWriteResult.Unmapped? = null

    override fun write(template: ByteArray, entries: List<SiagieGradeEntry>): SiagieGradesWriteResult =
        unmapped ?: SiagieGradesWriteResult.Written(template)
}

class FakeExportStore : SiagieExportStore {

    override suspend fun write(fileName: String, content: ByteArray): ExportedFile =
        ExportedFile(name = fileName, path = "/cache/exports/$fileName")
}

class FakeSummaryDocuments : SummaryDocuments {

    override suspend fun write(fileName: String, bytes: ByteArray): SummaryFile =
        SummaryFile(name = fileName, path = "/cache/exports/$fileName")
}

class FakePeriodLevelSummaryPdfRenderer : PeriodLevelSummaryPdfRenderer {

    override fun render(sectionTitle: String, periodLabel: String, summary: PeriodLevelSummary): ByteArray =
        byteArrayOf(1, 2, 3)
}

class FakeAttendanceRepository : AttendanceRepository {

    private val records: MutableStateFlow<List<AttendanceRecord>> = MutableStateFlow(emptyList())

    override fun observeBySectionAndDate(sectionId: SectionId, date: LocalDate): Flow<List<AttendanceRecord>> =
        records.map { stored -> stored.filter { it.sectionId == sectionId && it.date == date } }

    override fun observeBySectionAndMonth(sectionId: SectionId, month: YearMonth): Flow<List<AttendanceRecord>> =
        records.map { stored ->
            stored.filter { it.sectionId == sectionId && YearMonth.from(it.date) == month }
        }

    override suspend fun record(record: AttendanceRecord) {
        records.value = records.value.filterNot {
            it.sectionId == record.sectionId && it.studentId == record.studentId && it.date == record.date
        } + record
    }

    override suspend fun countRecordedDays(sectionId: SectionId): Int =
        records.value.filter { it.sectionId == sectionId }.map { it.date }.distinct().size

    override suspend fun deleteBySection(sectionId: SectionId) = Unit
}

class FakeMonthlyAttendanceExporter : MonthlyAttendanceExporter {

    var shouldFail: Boolean = false

    override suspend fun export(
        templateUri: String,
        month: YearMonth,
        entries: List<AttendanceExportEntry>,
    ): AttendanceExportFile {
        if (shouldFail) error("export failed")
        return AttendanceExportFile(
            fileName = "asistencia.xlsx",
            path = "/cache/exports/asistencia.xlsx",
        )
    }
}
