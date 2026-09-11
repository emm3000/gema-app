package com.emm.gema.core.siagie

import com.emm.gema.core.domain.curriculum.Competency
import com.emm.gema.core.domain.curriculum.GetPeriodCompetenciesUseCase
import com.emm.gema.core.domain.evaluation.AchievementLevel
import com.emm.gema.core.domain.evaluation.GetPeriodLevelGridUseCase
import com.emm.gema.core.domain.evaluation.PeriodLevel
import com.emm.gema.core.domain.evaluation.PeriodLevelKey
import com.emm.gema.core.domain.evaluation.UnworkedComment
import com.emm.gema.core.domain.export.ExportGradesUseCase
import com.emm.gema.core.domain.export.ExportedFile
import com.emm.gema.core.domain.export.GetGradesExportPlanUseCase
import com.emm.gema.core.domain.export.GradesExportResult
import com.emm.gema.core.domain.section.Area
import com.emm.gema.core.domain.section.GetSectionAreasUseCase
import com.emm.gema.core.domain.section.Grade
import com.emm.gema.core.domain.section.Section
import com.emm.gema.core.domain.siagie.ApplySiagieImportUseCase
import com.emm.gema.core.domain.siagie.SiagieImportPlanner
import com.emm.gema.core.domain.student.Student
import com.emm.gema.core.domain.student.StudentCode
import com.google.common.truth.Truth.assertThat
import java.io.File
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.zip.ZipFile
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

private const val SECTION_ID: String = "section-1"
private const val PERIOD_ID: String = "period-1"
private const val FIXTURE_NAME: String = "6 Primaria EBR.xlsx"
private const val COMU_SHEET_PART: String = "xl/worksheets/sheet1.xml"

class SiagieGradesExportFixtureTest {

    @get:Rule
    val temporaryFolder: TemporaryFolder = TemporaryFolder()

    private val sections = FakeSectionRepository()
    private val students = FakeStudentRepository()
    private val importStore = FakeSiagieImportStore(students)
    private val competencies = FakeCompetencyRepository(
        listOf(competencyOf(Area.COMU, 1), competencyOf(Area.COMU, 2), competencyOf(Area.MATE, 1)),
    )
    private val worked = FakeWorkedCompetencyRepository()
    private val levels = FakePeriodLevelRepository()
    private val sectionAreas = FakeSectionAreaRepository()

    private val applyImport = ApplySiagieImportUseCase(
        planner = SiagieImportPlanner(sections, students, FileSiagieDocuments(), XlsxSiagieRosterReader()),
        students = students,
        store = importStore,
        idGenerator = SequentialIdGenerator(),
        clock = Clock.fixed(Instant.parse("2026-09-10T12:00:00Z"), ZoneOffset.UTC),
    )

    private val getPlan = GetGradesExportPlanUseCase(
        getSectionAreas = GetSectionAreasUseCase(sectionAreas),
        getPeriodLevelGrid = GetPeriodLevelGridUseCase(
            getPeriodCompetencies = GetPeriodCompetenciesUseCase(competencies, worked),
            studentRepository = students,
            periodLevelRepository = levels,
        ),
    )

    @Test
    fun `the export fills the imported template and keeps its name`() = runTest {
        importFixture()
        worked.setWorked(SECTION_ID, PERIOD_ID, Competency.idOf(Area.COMU, 1), true)
        record("student-1", Area.COMU, 1, AchievementLevel.AD, "Lee con fluidez")
        record("student-2", Area.COMU, 1, AchievementLevel.C, "Requiere acompanamiento")
        recordComment("student-3", Area.COMU, 1, UnworkedComment.NOT_ENOUGH_EVIDENCE)

        val result: GradesExportResult = exportGrades()(SECTION_ID, PERIOD_ID)

        val file: ExportedFile = (result as GradesExportResult.Exported).file
        assertThat(file.name).isEqualTo(FIXTURE_NAME)
        val cells: Map<String, String> = XlsxTemplate(File(file.path)).readSheet("COMU")
        assertThat(cells["D4"]).isEqualTo("AD")
        assertThat(cells["E4"]).isEqualTo("Lee con fluidez")
        assertThat(cells["D5"]).isEqualTo("C")
        assertThat(cells["E5"]).isEqualTo("Requiere acompanamiento")
        assertThat(cells["D6"]).isEqualTo("Comentario 2")
    }

    @Test
    fun `a competency that was not worked keeps its cells empty`() = runTest {
        importFixture()
        worked.setWorked(SECTION_ID, PERIOD_ID, Competency.idOf(Area.COMU, 1), true)
        record("student-1", Area.COMU, 1, AchievementLevel.A, "")
        record("student-1", Area.COMU, 2, AchievementLevel.B, "")

        val result: GradesExportResult = exportGrades()(SECTION_ID, PERIOD_ID)

        val cells: Map<String, String> = XlsxTemplate(File(exportedPath(result))).readSheet("COMU")
        assertThat(cells["D4"]).isEqualTo("A")
        assertThat(cells).doesNotContainKey("F4")
    }

    @Test
    fun `every part but the edited sheet stays byte for byte`() = runTest {
        importFixture()
        worked.setWorked(SECTION_ID, PERIOD_ID, Competency.idOf(Area.COMU, 1), true)
        record("student-1", Area.COMU, 1, AchievementLevel.A, "")

        val result: GradesExportResult = exportGrades()(SECTION_ID, PERIOD_ID)

        val original: Map<String, ByteArray> = parts(fixture())
        val written: Map<String, ByteArray> = parts(File(exportedPath(result)))
        assertThat(written.keys).containsExactlyElementsIn(original.keys).inOrder()
        original.filterKeys { it != COMU_SHEET_PART }.forEach { (name, payload) ->
            assertThat(written[name]).isEqualTo(payload)
        }
    }

    @Test
    fun `every cell of the edited sheet but the written ones stays as it was`() = runTest {
        importFixture()
        worked.setWorked(SECTION_ID, PERIOD_ID, Competency.idOf(Area.COMU, 1), true)
        record("student-1", Area.COMU, 1, AchievementLevel.AD, "Lee con fluidez")
        record("student-2", Area.COMU, 1, AchievementLevel.B, "")

        val result: GradesExportResult = exportGrades()(SECTION_ID, PERIOD_ID)

        val written: Set<String> = setOf("D4", "E4", "D5")
        val before: Map<String, String> = XlsxTemplate(fixture()).readSheet("COMU")
        val after: Map<String, String> = XlsxTemplate(File(exportedPath(result))).readSheet("COMU")
        assertThat(after.filterKeys { it !in written }).containsExactlyEntriesIn(before)
        assertThat(after.filterKeys { it in written })
            .containsExactly("D4", "AD", "E4", "Lee con fluidez", "D5", "B")
    }

    @Test
    fun `a C without a descriptive conclusion blocks the whole file`() = runTest {
        importFixture()
        worked.setWorked(SECTION_ID, PERIOD_ID, Competency.idOf(Area.COMU, 1), true)
        record("student-1", Area.COMU, 1, AchievementLevel.C, "")

        val result: GradesExportResult = exportGrades()(SECTION_ID, PERIOD_ID)

        val blocked = result as GradesExportResult.Blocked
        assertThat(blocked.gaps.map { it.studentName }).containsExactly("ALVARADO QUISPE, MARIA FERNANDA")
        assertThat(temporaryFolder.root.listFiles()?.toList().orEmpty()).isEmpty()
    }

    @Test
    fun `a section without a stored template cannot export`() = runTest {
        sections.section = Section(id = SECTION_ID, schoolYearId = "year-1", grade = Grade.SIXTH, name = "A")
        students.save(
            Student(
                id = "student-1",
                sectionId = SECTION_ID,
                code = StudentCode("10000000000001"),
                fullName = "ALVARADO QUISPE, MARIA FERNANDA",
            ),
        )

        val result: GradesExportResult = exportGrades()(SECTION_ID, PERIOD_ID)

        assertThat(result).isEqualTo(GradesExportResult.Unavailable)
    }

    private fun exportGrades(): ExportGradesUseCase = ExportGradesUseCase(
        getPlan = getPlan,
        importStore = importStore,
        writer = XlsxSiagieGradesWriter(),
        exportStore = FileExportStore(temporaryFolder.root),
        students = students,
    )

    private suspend fun importFixture() {
        sections.section = Section(id = SECTION_ID, schoolYearId = "year-1", grade = Grade.SIXTH, name = "A")
        applyImport(SECTION_ID, fixture().absolutePath, emptySet(), LocalDate.of(2026, 9, 10))
    }

    private suspend fun record(
        studentId: String,
        area: Area,
        ordinal: Int,
        level: AchievementLevel,
        conclusion: String,
    ) {
        levels.save(
            PeriodLevel(
                key = keyOf(studentId, area, ordinal),
                achievementLevel = level,
                descriptiveConclusion = conclusion,
            ),
        )
    }

    private suspend fun recordComment(studentId: String, area: Area, ordinal: Int, comment: UnworkedComment) {
        levels.save(PeriodLevel(key = keyOf(studentId, area, ordinal), unworkedComment = comment))
    }

    private fun keyOf(studentId: String, area: Area, ordinal: Int): PeriodLevelKey = PeriodLevelKey(
        sectionId = SECTION_ID,
        periodId = PERIOD_ID,
        studentId = studentId,
        competencyId = Competency.idOf(area, ordinal),
    )

    private fun exportedPath(result: GradesExportResult): String =
        (result as GradesExportResult.Exported).file.path

    private fun fixture(): File = File(requireNotNull(javaClass.classLoader.getResource(FIXTURE_NAME)).toURI())

    private fun parts(file: File): Map<String, ByteArray> = ZipFile(file).use { zip ->
        zip.entries().toList().associate { entry ->
            entry.name to zip.getInputStream(entry).use { it.readBytes() }
        }
    }
}

private fun competencyOf(area: Area, ordinal: Int): Competency = Competency(
    id = Competency.idOf(area, ordinal),
    area = area,
    siagieOrdinal = ordinal,
    name = "Competencia $ordinal",
)
