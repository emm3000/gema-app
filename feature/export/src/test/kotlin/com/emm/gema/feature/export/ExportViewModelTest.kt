package com.emm.gema.feature.export

import app.cash.turbine.test
import com.emm.gema.core.domain.curriculum.Competency
import com.emm.gema.core.domain.curriculum.GetPeriodCompetenciesUseCase
import com.emm.gema.core.domain.evaluation.AchievementLevel
import com.emm.gema.core.domain.evaluation.ExportPeriodLevelSummaryUseCase
import com.emm.gema.core.domain.evaluation.GetPeriodLevelGridUseCase
import com.emm.gema.core.domain.evaluation.GetPeriodLevelSummaryUseCase
import com.emm.gema.core.domain.evaluation.PeriodLevel
import com.emm.gema.core.domain.evaluation.PeriodLevelKey
import com.emm.gema.core.domain.export.ExportGradesUseCase
import com.emm.gema.core.domain.export.GetGradesExportPlanUseCase
import com.emm.gema.core.domain.export.GetGradesTemplateNameUseCase
import com.emm.gema.core.domain.export.SIAGIE_GRADES_MIME_TYPE
import com.emm.gema.core.domain.schoolyear.GetCurrentPeriodUseCase
import com.emm.gema.core.domain.schoolyear.GetPeriodsUseCase
import com.emm.gema.core.domain.schoolyear.GetSchoolYearUseCase
import com.emm.gema.core.domain.schoolyear.Period
import com.emm.gema.core.domain.schoolyear.PeriodKind
import com.emm.gema.core.domain.schoolyear.SchoolYear
import com.emm.gema.core.domain.section.Area
import com.emm.gema.core.domain.section.GetSectionAreasUseCase
import com.emm.gema.core.domain.section.GetSectionUseCase
import com.emm.gema.core.domain.section.Grade
import com.emm.gema.core.domain.section.Section
import com.emm.gema.core.domain.siagie.ImportedTemplate
import com.emm.gema.core.domain.siagie.ImportedTemplateKind
import com.emm.gema.core.domain.siagie.SiagieCompetencyColumn
import com.emm.gema.core.domain.siagie.SiagieGradesWriteResult
import com.emm.gema.core.domain.student.Student
import com.emm.gema.core.domain.student.StudentCode
import com.google.common.truth.Truth.assertThat
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

private const val SECTION_ID: String = "section-1"
private const val PERIOD_ID: String = "period-1"
private const val TEMPLATE_NAME: String = "6 Primaria EBR.xlsx"

class ExportViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val section = Section(id = SECTION_ID, schoolYearId = "year-1", grade = Grade.SIXTH, name = "A")
    private val schoolYear = SchoolYear(
        id = "year-1",
        label = "2026",
        startDate = LocalDate.of(2026, 3, 1),
        endDate = LocalDate.of(2026, 12, 20),
        periodKind = PeriodKind.BIMESTER,
    )
    private val periods: List<Period> = listOf(
        Period(
            id = PERIOD_ID,
            schoolYearId = "year-1",
            number = 1,
            startDate = LocalDate.of(2026, 3, 1),
            endDate = LocalDate.of(2026, 5, 31),
        ),
    )

    private val students = FakeStudentRepository()
    private val levels = FakePeriodLevelRepository()
    private val worked = FakeWorkedCompetencyRepository()
    private val sectionAreas = FakeSectionAreaRepository()
    private val importStore = FakeSiagieImportStore()
    private val competencies = FakeCompetencyRepository(listOf(competency))
    private val writer = FakeGradesWriter()

    @Test
    fun `a section without a stored template offers the import instead`() = runTest {
        seedSection()

        val viewModel: ExportViewModel = viewModel()

        assertThat(viewModel.state.value.gradesExportState).isEqualTo(GradesExportUiState.Unavailable)
        assertThat(viewModel.state.value.templateFileName).isNull()
    }

    @Test
    fun `a C without a descriptive conclusion blocks the file and lists the student`() = runTest {
        seedSection()
        storeTemplate()
        record(AchievementLevel.C, conclusion = "")

        val viewModel: ExportViewModel = viewModel()

        val blocked = viewModel.state.value.gradesExportState as GradesExportUiState.Blocked
        assertThat(blocked.gaps).containsExactly(
            ExportGapRow(
                studentId = "student-1",
                studentName = "ALVARADO QUISPE, MARIA",
                competencyId = "COMU-1",
                competencyLabel = "Comunicación - 01",
            ),
        )
    }

    @Test
    fun `a complete period hands the file to the share sheet`() = runTest {
        seedSection()
        storeTemplate()
        record(AchievementLevel.A, conclusion = "")
        val viewModel: ExportViewModel = viewModel()

        viewModel.effects.test {
            viewModel.onIntent(ExportUiIntent.ExportGradesClicked)

            val shared = awaitItem() as ExportUiEffect.ShareFile
            assertThat(shared.path).isEqualTo("/cache/exports/$TEMPLATE_NAME")
            assertThat(shared.mimeType).isEqualTo(SIAGIE_GRADES_MIME_TYPE)
        }
        assertThat(viewModel.state.value.gradesExportState).isEqualTo(GradesExportUiState.Ready)
    }

    @Test
    fun `a gap row opens the cell that fixes it`() = runTest {
        seedSection()
        storeTemplate()
        record(AchievementLevel.C, conclusion = "")
        val viewModel: ExportViewModel = viewModel()
        val gap: ExportGapRow = (viewModel.state.value.gradesExportState as GradesExportUiState.Blocked).gaps.first()

        viewModel.effects.test {
            viewModel.onIntent(ExportUiIntent.GapRowClicked(gap))

            assertThat(awaitItem()).isEqualTo(
                ExportUiEffect.NavigateToPeriodLevelCell(
                    sectionId = SECTION_ID,
                    studentId = "student-1",
                    competencyId = "COMU-1",
                ),
            )
        }
    }

    @Test
    fun `a template that cannot hold every level is reported instead of filled`() = runTest {
        seedSection()
        storeTemplate()
        record(AchievementLevel.A, conclusion = "")
        writer.unmapped = SiagieGradesWriteResult.Unmapped(
            areas = listOf(Area.MATE),
            studentCodes = listOf(StudentCode("10000000000001")),
            competencies = listOf(SiagieCompetencyColumn(area = Area.COMU, siagieOrdinal = 3)),
        )
        val viewModel: ExportViewModel = viewModel()

        viewModel.onIntent(ExportUiIntent.ExportGradesClicked)

        assertThat(viewModel.state.value.templateMismatch).isEqualTo(
            TemplateMismatchUi(
                areaNames = listOf("Matemática"),
                studentNames = listOf("ALVARADO QUISPE, MARIA"),
                competencyLabels = listOf("Comunicación - 03"),
            ),
        )
    }

    @Test
    fun `the period picker starts on the period that contains today`() = runTest {
        seedSection()
        storeTemplate()

        val viewModel: ExportViewModel = viewModel()

        assertThat(viewModel.state.value.selectedPeriodId).isEqualTo(PERIOD_ID)
        assertThat(viewModel.state.value.periods.single().label).isEqualTo("I Bimestre")
        assertThat(viewModel.state.value.sectionTitle).isEqualTo("6° A")
    }

    @Test
    fun `exporting the summary as csv hands the file to the share sheet`() = runTest {
        seedSection()
        val viewModel: ExportViewModel = viewModel()

        viewModel.effects.test {
            viewModel.onIntent(ExportUiIntent.ExportSummaryCsvClicked)

            val shared = awaitItem() as ExportUiEffect.ShareFile
            assertThat(shared.mimeType).isEqualTo("text/csv")
        }
    }

    @Test
    fun `exporting the summary as pdf hands the file to the share sheet`() = runTest {
        seedSection()
        val viewModel: ExportViewModel = viewModel()

        viewModel.effects.test {
            viewModel.onIntent(ExportUiIntent.ExportSummaryPdfClicked)

            val shared = awaitItem() as ExportUiEffect.ShareFile
            assertThat(shared.mimeType).isEqualTo("application/pdf")
        }
    }

    @Test
    fun `activeExport is null once a summary export finishes`() = runTest {
        seedSection()
        val viewModel: ExportViewModel = viewModel()

        viewModel.effects.test {
            viewModel.onIntent(ExportUiIntent.ExportSummaryCsvClicked)
            awaitItem()
        }

        assertThat(viewModel.state.value.activeExport).isNull()
    }

    private fun viewModel(): ExportViewModel {
        val getPlan = GetGradesExportPlanUseCase(
            getSectionAreas = GetSectionAreasUseCase(sectionAreas),
            getPeriodLevelGrid = GetPeriodLevelGridUseCase(
                getPeriodCompetencies = GetPeriodCompetenciesUseCase(competencies, worked),
                studentRepository = students,
                periodLevelRepository = levels,
            ),
        )

        return ExportViewModel(
            sectionId = SECTION_ID,
            getSection = GetSectionUseCase(FakeSectionRepository(section)),
            getSchoolYear = GetSchoolYearUseCase(FakeSchoolYearRepository(schoolYear)),
            getPeriods = GetPeriodsUseCase(FakePeriodRepository(periods)),
            getCurrentPeriod = GetCurrentPeriodUseCase(
                FakePeriodRepository(periods),
                Clock.fixed(Instant.parse("2026-04-15T10:00:00Z"), ZoneOffset.UTC),
            ),
            getGradesTemplateName = GetGradesTemplateNameUseCase(importStore),
            getGradesExportPlan = getPlan,
            exportGrades = ExportGradesUseCase(
                getPlan = getPlan,
                importStore = importStore,
                writer = writer,
                exportStore = FakeExportStore(),
                students = students,
            ),
            exportPeriodLevelSummary = ExportPeriodLevelSummaryUseCase(
                getSummary = GetPeriodLevelSummaryUseCase(
                    sectionAreaRepository = sectionAreas,
                    competencyRepository = competencies,
                    workedCompetencyRepository = worked,
                    studentRepository = students,
                    periodLevelRepository = levels,
                ),
                documents = FakeSummaryDocuments(),
                pdfRenderer = FakePeriodLevelSummaryPdfRenderer(),
            ),
        )
    }

    private suspend fun seedSection() {
        students.save(
            Student(
                id = "student-1",
                sectionId = SECTION_ID,
                code = StudentCode("10000000000001"),
                fullName = "ALVARADO QUISPE, MARIA",
            ),
        )
        worked.setWorked(SECTION_ID, PERIOD_ID, competency.id, true)
    }

    private suspend fun storeTemplate() {
        importStore.apply(
            students = emptyList(),
            template = ImportedTemplate(
                sectionId = SECTION_ID,
                kind = ImportedTemplateKind.GRADES,
                fileName = TEMPLATE_NAME,
                content = byteArrayOf(1),
                importedAt = Instant.parse("2026-09-10T12:00:00Z"),
            ),
        )
    }

    private suspend fun record(level: AchievementLevel, conclusion: String) {
        levels.save(
            PeriodLevel(
                key = PeriodLevelKey(SECTION_ID, PERIOD_ID, "student-1", competency.id),
                achievementLevel = level,
                descriptiveConclusion = conclusion,
            ),
        )
    }
}

private val competency: Competency = Competency(
    id = Competency.idOf(Area.COMU, 1),
    area = Area.COMU,
    siagieOrdinal = 1,
    name = "Se comunica oralmente en su lengua materna",
)
