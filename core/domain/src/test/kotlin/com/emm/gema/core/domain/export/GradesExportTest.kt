package com.emm.gema.core.domain.export

import com.emm.gema.core.domain.curriculum.Competency
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
import com.emm.gema.core.domain.section.Area
import com.emm.gema.core.domain.section.GetSectionAreasUseCase
import com.emm.gema.core.domain.siagie.ImportedTemplate
import com.emm.gema.core.domain.siagie.ImportedTemplateKind
import com.emm.gema.core.domain.siagie.SiagieGradeEntry
import com.emm.gema.core.domain.siagie.SiagieGradesWriteResult
import com.emm.gema.core.domain.siagie.SiagieGradesWriter
import com.emm.gema.core.domain.student.Student
import com.emm.gema.core.domain.student.StudentCode
import com.google.common.truth.Truth.assertThat
import java.time.Instant
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

private const val SECTION_ID: String = "section-1"
private const val PERIOD_ID: String = "period-1"
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
        record("student-1", "COMU-1", AchievementLevel.C)
        record("student-2", "COMU-1", AchievementLevel.A)

        val plan: GradesExportPlan = getPlan(SECTION_ID, PERIOD_ID).first()

        assertThat(plan.isReady).isFalse()
        assertThat(plan.gaps).containsExactly(
            ExportGap(
                studentId = "student-1",
                studentName = "ALVARADO QUISPE, MARIA",
                competency = competencyOf(Area.COMU, 1),
            ),
        )
    }

    @Test
    fun `every offending student and competency is listed`() = runTest {
        seedSection()
        record("student-1", "COMU-1", AchievementLevel.C)
        record("student-1", "MATE-1", AchievementLevel.C)
        record("student-2", "MATE-1", AchievementLevel.C, conclusion = "Avanza con apoyo")

        val plan: GradesExportPlan = getPlan(SECTION_ID, PERIOD_ID).first()

        assertThat(plan.gaps.map { it.studentId to it.competency.id })
            .containsExactly("student-1" to "COMU-1", "student-1" to "MATE-1")
    }

    @Test
    fun `no file is produced while a gap exists`() = runTest {
        seedSection()
        storeTemplate()
        record("student-1", "COMU-1", AchievementLevel.C)

        val result: GradesExportResult = exportGrades(SECTION_ID, PERIOD_ID)

        assertThat(result).isInstanceOf(GradesExportResult.Blocked::class.java)
        assertThat(writer.calls).isEmpty()
        assertThat(exportStore.written).isEmpty()
    }

    @Test
    fun `a section without a stored template cannot export`() = runTest {
        seedSection()
        record("student-1", "COMU-1", AchievementLevel.A)

        val result: GradesExportResult = exportGrades(SECTION_ID, PERIOD_ID)

        assertThat(result).isEqualTo(GradesExportResult.Unavailable)
        assertThat(writer.calls).isEmpty()
    }

    @Test
    fun `only worked competencies of active areas reach the writer`() = runTest {
        seedSection()
        storeTemplate()
        sectionAreas.setAreaHidden(SECTION_ID, Area.MATE, true)
        record("student-1", "COMU-1", AchievementLevel.AD, conclusion = "Lee con fluidez")
        record("student-1", "MATE-1", AchievementLevel.A)
        record("student-2", "COMU-1", unworkedComment = UnworkedComment.NOT_ENOUGH_EVIDENCE)

        val result: GradesExportResult = exportGrades(SECTION_ID, PERIOD_ID)

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
        record("student-1", "COMU-1", AchievementLevel.B)

        exportGrades(SECTION_ID, PERIOD_ID)

        assertThat(exportStore.written.single()).isEqualTo(TEMPLATE_NAME)
    }

    @Test
    fun `a competency that was not worked is never exported`() = runTest {
        seedSection()
        storeTemplate()
        record("student-1", "COMU-2", AchievementLevel.A)

        val plan: GradesExportPlan = getPlan(SECTION_ID, PERIOD_ID).first()

        assertThat(plan.entries.map { it.siagieOrdinal }).doesNotContain(2)
        assertThat(plan.entries).isEmpty()
    }

    @Test
    fun `a template missing an area or a student blocks the export`() = runTest {
        seedSection()
        storeTemplate()
        record("student-1", "COMU-1", AchievementLevel.A)
        writer.unmapped = SiagieGradesWriteResult.Unmapped(
            areas = listOf(Area.MATE),
            studentCodes = listOf(StudentCode("10000000000002")),
        )

        val result: GradesExportResult = exportGrades(SECTION_ID, PERIOD_ID)

        assertThat(result).isEqualTo(
            GradesExportResult.TemplateMismatch(
                areas = listOf(Area.MATE),
                studentNames = listOf("BAUTISTA HUAMAN, JOSE"),
            ),
        )
        assertThat(exportStore.written).isEmpty()
    }

    private suspend fun seedSection() {
        competencies.seed(
            listOf(competencyOf(Area.COMU, 1), competencyOf(Area.COMU, 2), competencyOf(Area.MATE, 1)),
            curriculumVersion = 1,
        )
        worked.setWorked(SECTION_ID, PERIOD_ID, "COMU-1", true)
        worked.setWorked(SECTION_ID, PERIOD_ID, "MATE-1", true)
        students.save(studentOf("student-1", "10000000000001", "ALVARADO QUISPE, MARIA"))
        students.save(studentOf("student-2", "10000000000002", "BAUTISTA HUAMAN, JOSE"))
    }

    private suspend fun storeTemplate() {
        importStore.apply(
            students = emptyList(),
            template = ImportedTemplate(
                sectionId = SECTION_ID,
                kind = ImportedTemplateKind.GRADES,
                fileName = TEMPLATE_NAME,
                content = byteArrayOf(1, 2, 3),
                importedAt = Instant.parse("2026-09-10T12:00:00Z"),
            ),
        )
    }

    private suspend fun record(
        studentId: String,
        competencyId: String,
        level: AchievementLevel? = null,
        unworkedComment: UnworkedComment? = null,
        conclusion: String = "",
    ) {
        levels.save(
            PeriodLevel(
                key = PeriodLevelKey(SECTION_ID, PERIOD_ID, studentId, competencyId),
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
        id = id,
        sectionId = SECTION_ID,
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
