package com.emm.gema.core.domain.export

import com.emm.gema.core.domain.curriculum.Competency
import com.emm.gema.core.domain.curriculum.CompetencyId
import com.emm.gema.core.domain.curriculum.GetPeriodCompetenciesUseCase
import com.emm.gema.core.domain.evaluation.AchievementLevel
import com.emm.gema.core.domain.evaluation.GetPeriodLevelGridUseCase
import com.emm.gema.core.domain.evaluation.PeriodLevel
import com.emm.gema.core.domain.evaluation.PeriodLevelKey
import com.emm.gema.core.domain.evaluation.UnworkedComment
import com.emm.gema.core.domain.fake.InMemoryCompetencyRepository
import com.emm.gema.core.domain.fake.InMemoryPeriodLevelRepository
import com.emm.gema.core.domain.fake.InMemorySectionAreaRepository
import com.emm.gema.core.domain.fake.InMemorySiagieImportStore
import com.emm.gema.core.domain.fake.InMemoryStudentRepository
import com.emm.gema.core.domain.fake.InMemoryWorkedCompetencyRepository
import com.emm.gema.core.domain.schoolyear.PeriodId
import com.emm.gema.core.domain.section.Area
import com.emm.gema.core.domain.section.GetSectionAreasUseCase
import com.emm.gema.core.domain.section.SectionId
import com.emm.gema.core.domain.siagie.ImportedTemplate
import com.emm.gema.core.domain.siagie.ImportedTemplateKind
import com.emm.gema.core.domain.siagie.SiagieCompetencyColumn
import com.emm.gema.core.domain.siagie.SiagieGradeEntry
import com.emm.gema.core.domain.siagie.SiagieGradesWriteResult
import com.emm.gema.core.domain.siagie.SiagieGradesWriter
import com.emm.gema.core.domain.student.Student
import com.emm.gema.core.domain.student.StudentCode
import com.emm.gema.core.domain.student.StudentId
import com.google.common.truth.Truth.assertThat
import java.time.Instant
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

private val sectionId: SectionId = SectionId("section-1")
private val periodId: PeriodId = PeriodId("period-1")
private const val TEMPLATE_NAME: String = "6 Primaria EBR.xlsx"

class GradesExportTest {

    private val competencies = InMemoryCompetencyRepository()
    private val worked = InMemoryWorkedCompetencyRepository()
    private val students = InMemoryStudentRepository()
    private val levels = InMemoryPeriodLevelRepository()
    private val sectionAreas = InMemorySectionAreaRepository()
    private val importStore = InMemorySiagieImportStore(students)
    private val writer = RecordingGradesWriter()
    private val exportStore = RecordingExportStore()

    private val getPlan = GetGradesExportPlanUseCase(
        getSectionAreas = GetSectionAreasUseCase(sectionAreas),
        getPeriodLevelGrid = GetPeriodLevelGridUseCase(
            getPeriodCompetencies = GetPeriodCompetenciesUseCase(competencies, worked),
            studentRepository = students,
            periodLevelRepository = levels,
        ),
    )

    private val exportGrades = ExportGradesUseCase(
        getPlan = getPlan,
        importStore = importStore,
        writer = writer,
        exportStore = exportStore,
        students = students,
    )

    @Test
    fun `a C without a descriptive conclusion blocks the export`() = runTest {
        seedSection()
        record(firstStudentId, firstComuId, AchievementLevel.C)
        record(secondStudentId, firstComuId, AchievementLevel.A)

        val plan: GradesExportPlan = getPlan(sectionId, periodId).first()

        assertThat(plan.isReady).isFalse()
        assertThat(plan.gaps).containsExactly(
            ExportGap(
                studentId = firstStudentId,
                studentName = "ALVARADO QUISPE, MARIA",
                competency = competencyOf(Area.COMU, 1),
            ),
        )
    }

    @Test
    fun `every offending student and competency is listed`() = runTest {
        seedSection()
        record(firstStudentId, firstComuId, AchievementLevel.C)
        record(firstStudentId, firstMateId, AchievementLevel.C)
        record(secondStudentId, firstMateId, AchievementLevel.C, conclusion = "Avanza con apoyo")

        val plan: GradesExportPlan = getPlan(sectionId, periodId).first()

        assertThat(plan.gaps.map { it.studentId to it.competency.id })
            .containsExactly(firstStudentId to firstComuId, firstStudentId to firstMateId)
    }

    @Test
    fun `no file is produced while a gap exists`() = runTest {
        seedSection()
        storeTemplate()
        record(firstStudentId, firstComuId, AchievementLevel.C)

        val result: GradesExportResult = exportGrades(sectionId, periodId)

        assertThat(result).isInstanceOf(GradesExportResult.Blocked::class.java)
        assertThat(writer.calls).isEmpty()
        assertThat(exportStore.written).isEmpty()
    }

    @Test
    fun `a section without a stored template cannot export`() = runTest {
        seedSection()
        record(firstStudentId, firstComuId, AchievementLevel.A)

        val result: GradesExportResult = exportGrades(sectionId, periodId)

        assertThat(result).isEqualTo(GradesExportResult.Unavailable)
        assertThat(writer.calls).isEmpty()
    }

    @Test
    fun `only worked competencies of active areas reach the writer`() = runTest {
        seedSection()
        storeTemplate()
        sectionAreas.setAreaHidden(sectionId, Area.MATE, true)
        record(firstStudentId, firstComuId, AchievementLevel.AD, conclusion = "Lee con fluidez")
        record(firstStudentId, firstMateId, AchievementLevel.A)
        record(secondStudentId, firstComuId, unworkedComment = UnworkedComment.NOT_ENOUGH_EVIDENCE)

        val result: GradesExportResult = exportGrades(sectionId, periodId)

        assertThat(result).isEqualTo(
            GradesExportResult.Exported(ExportedFile(name = TEMPLATE_NAME, path = "/cache/$TEMPLATE_NAME")),
        )
        assertThat(writer.calls.single()).containsExactly(
            SiagieGradeEntry(
                area = Area.COMU,
                siagieOrdinal = 1,
                studentCode = StudentCode("10000000000001"),
                achievementValue = "AD",
                descriptiveConclusion = "Lee con fluidez",
            ),
            SiagieGradeEntry(
                area = Area.COMU,
                siagieOrdinal = 1,
                studentCode = StudentCode("10000000000002"),
                achievementValue = "Comentario 2",
                descriptiveConclusion = "",
            ),
        )
    }

    @Test
    fun `the exported file keeps the imported file name`() = runTest {
        seedSection()
        storeTemplate()
        record(firstStudentId, firstComuId, AchievementLevel.B)

        exportGrades(sectionId, periodId)

        assertThat(exportStore.written.single()).isEqualTo(TEMPLATE_NAME)
    }

    @Test
    fun `a competency that was not worked is never exported`() = runTest {
        seedSection()
        storeTemplate()
        record(firstStudentId, secondComuId, AchievementLevel.A)

        val plan: GradesExportPlan = getPlan(sectionId, periodId).first()

        assertThat(plan.entries.map { it.siagieOrdinal }).doesNotContain(2)
        assertThat(plan.entries).isEmpty()
    }

    @Test
    fun `a template missing an area or a student blocks the export`() = runTest {
        seedSection()
        storeTemplate()
        record(firstStudentId, firstComuId, AchievementLevel.A)
        writer.unmapped = SiagieGradesWriteResult.Unmapped(
            areas = listOf(Area.MATE),
            studentCodes = listOf(StudentCode("10000000000002")),
            competencies = emptyList(),
        )

        val result: GradesExportResult = exportGrades(sectionId, periodId)

        assertThat(result).isEqualTo(
            GradesExportResult.TemplateMismatch(
                areas = listOf(Area.MATE),
                studentNames = listOf("BAUTISTA HUAMAN, JOSE"),
                competencies = emptyList(),
            ),
        )
        assertThat(exportStore.written).isEmpty()
    }

    @Test
    fun `a template missing a competency column blocks the export`() = runTest {
        seedSection()
        storeTemplate()
        record(firstStudentId, firstComuId, AchievementLevel.A)
        writer.unmapped = SiagieGradesWriteResult.Unmapped(
            areas = emptyList(),
            studentCodes = emptyList(),
            competencies = listOf(SiagieCompetencyColumn(area = Area.COMU, siagieOrdinal = 1)),
        )

        val result: GradesExportResult = exportGrades(sectionId, periodId)

        assertThat(result).isEqualTo(
            GradesExportResult.TemplateMismatch(
                areas = emptyList(),
                studentNames = emptyList(),
                competencies = listOf(SiagieCompetencyColumn(area = Area.COMU, siagieOrdinal = 1)),
            ),
        )
        assertThat(exportStore.written).isEmpty()
    }

    private suspend fun seedSection() {
        competencies.seed(
            listOf(competencyOf(Area.COMU, 1), competencyOf(Area.COMU, 2), competencyOf(Area.MATE, 1)),
            curriculumVersion = 1,
        )
        worked.setWorked(sectionId, periodId, firstComuId, true)
        worked.setWorked(sectionId, periodId, firstMateId, true)
        students.save(studentOf("student-1", "10000000000001", "ALVARADO QUISPE, MARIA"))
        students.save(studentOf("student-2", "10000000000002", "BAUTISTA HUAMAN, JOSE"))
    }

    private suspend fun storeTemplate() {
        importStore.apply(
            students = emptyList(),
            template = ImportedTemplate(
                sectionId = sectionId,
                kind = ImportedTemplateKind.GRADES,
                fileName = TEMPLATE_NAME,
                content = byteArrayOf(1, 2, 3),
                importedAt = Instant.parse("2026-09-10T12:00:00Z"),
            ),
        )
    }

    private suspend fun record(
        studentId: StudentId,
        competencyId: CompetencyId,
        level: AchievementLevel? = null,
        unworkedComment: UnworkedComment? = null,
        conclusion: String = "",
    ) {
        levels.save(
            PeriodLevel(
                key = PeriodLevelKey(sectionId, periodId, studentId, competencyId),
                achievementLevel = level,
                unworkedComment = unworkedComment,
                descriptiveConclusion = conclusion,
            ),
        )
    }

    private fun competencyOf(area: Area, ordinal: Int): Competency = Competency(
        id = Competency.idOf(area, ordinal),
        area = area,
        siagieOrdinal = ordinal,
        name = "Competencia $ordinal de ${area.officialName}",
    )

    private fun studentOf(id: String, code: String, fullName: String): Student = Student(
        id = StudentId(id),
        sectionId = sectionId,
        code = StudentCode(code),
        fullName = fullName,
    )
}

private class RecordingGradesWriter : SiagieGradesWriter {

    val calls: MutableList<List<SiagieGradeEntry>> = mutableListOf()

    var unmapped: SiagieGradesWriteResult.Unmapped? = null

    override fun write(template: ByteArray, entries: List<SiagieGradeEntry>): SiagieGradesWriteResult {
        calls.add(entries)
        return unmapped ?: SiagieGradesWriteResult.Written(template + byteArrayOf(9))
    }
}

private class RecordingExportStore : SiagieExportStore {

    val written: MutableList<String> = mutableListOf()

    override suspend fun write(fileName: String, content: ByteArray): ExportedFile {
        written.add(fileName)
        return ExportedFile(name = fileName, path = "/cache/$fileName")
    }
}

private val firstStudentId: StudentId = StudentId("student-1")

private val secondStudentId: StudentId = StudentId("student-2")

private val firstComuId: CompetencyId = CompetencyId("COMU-1")

private val firstMateId: CompetencyId = CompetencyId("MATE-1")

private val secondComuId: CompetencyId = CompetencyId("COMU-2")
