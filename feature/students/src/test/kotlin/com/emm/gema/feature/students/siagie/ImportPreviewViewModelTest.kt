package com.emm.gema.feature.students.siagie

import app.cash.turbine.test
import com.emm.gema.core.domain.id.IdGenerator
import com.emm.gema.core.domain.schoolyear.SchoolYearId
import com.emm.gema.core.domain.section.GetSectionUseCase
import com.emm.gema.core.domain.section.Grade
import com.emm.gema.core.domain.section.Section
import com.emm.gema.core.domain.section.SectionId
import com.emm.gema.core.domain.siagie.ApplySiagieImportUseCase
import com.emm.gema.core.domain.siagie.PreviewSiagieImportUseCase
import com.emm.gema.core.domain.siagie.SiagieImportPlanner
import com.emm.gema.core.domain.siagie.SiagieRoster
import com.emm.gema.core.domain.siagie.SiagieRosterStudent
import com.emm.gema.core.domain.student.Student
import com.emm.gema.core.domain.student.StudentCode
import com.emm.gema.core.domain.student.StudentId
import com.emm.gema.feature.students.FakeSectionRepository
import com.emm.gema.feature.students.FakeSiagieDocuments
import com.emm.gema.feature.students.FakeSiagieImportStore
import com.emm.gema.feature.students.FakeSiagieRosterReader
import com.emm.gema.feature.students.FakeStudentRepository
import com.emm.gema.feature.students.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

private val sectionId: SectionId = SectionId("section-1")
private const val URI: String = "content://documents/6-primaria.xlsx"
private const val FILE_NAME: String = "6 Primaria EBR.xlsx"
private const val FIRST_CODE: String = "10000000000001"
private const val SECOND_CODE: String = "10000000000002"

class ImportPreviewViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val section = Section(id = sectionId, schoolYearId = SchoolYearId("2026"), grade = Grade.SIXTH, name = "A")
    private val sections = FakeSectionRepository(listOf(section))
    private val students = FakeStudentRepository()
    private val store = FakeSiagieImportStore(students)
    private val documents = FakeSiagieDocuments(FILE_NAME)
    private val reader = FakeSiagieRosterReader()
    private val clock: Clock = Clock.fixed(Instant.parse("2026-09-10T12:00:00Z"), ZoneOffset.UTC)

    @Test
    fun `the preview counts what the import will do`() = runTest {
        students.students.value = listOf(enrolled("student-1", FIRST_CODE, "ALVARADO QUISPE, MARIA"))
        reader.roster = rosterOf(
            entry(FIRST_CODE, "ALVARADO QUISPE, MARIA FERNANDA"),
            entry(SECOND_CODE, "BAUTISTA HUAMAN, JOSE"),
        )

        val state: ImportPreviewUiState = viewModel().state.value

        assertThat(state.fileName).isEqualTo(FILE_NAME)
        assertThat(state.sectionTitle).isEqualTo("6to A")
        assertThat(state.created.map { it.displayName }).containsExactly("BAUTISTA HUAMAN, JOSE")
        assertThat(state.updated.map { it.displayName }).containsExactly("ALVARADO QUISPE, MARIA FERNANDA")
        assertThat(state.proposedWithdrawals).isEmpty()
        assertThat(state.rejection).isNull()
    }

    @Test
    fun `a student missing from the template is proposed as withdrawn and selected`() = runTest {
        students.students.value = listOf(enrolled("student-1", SECOND_CODE, "BAUTISTA HUAMAN, JOSE"))
        reader.roster = rosterOf(entry(FIRST_CODE, "ALVARADO QUISPE, MARIA"))

        val state: ImportPreviewUiState = viewModel().state.value

        assertThat(state.proposedWithdrawals.single().displayName).isEqualTo("BAUTISTA HUAMAN, JOSE")
        assertThat(state.proposedWithdrawals.single().isSelected).isTrue()
    }

    @Test
    fun `a file that is not a SIAGIE template tells the teacher to pick another one`() = runTest {
        reader.roster = null

        val state: ImportPreviewUiState = viewModel().state.value

        assertThat(state.rejection?.instruction).isEqualTo("Elige otro archivo. No se cambió nada.")
    }

    @Test
    fun `an empty roster tells the teacher to pick another file`() = runTest {
        reader.roster = SiagieRoster(gradeNumber = 6, sectionName = null, students = emptyList())

        val state: ImportPreviewUiState = viewModel().state.value

        assertThat(state.rejection?.instruction).isEqualTo("Elige otro archivo. No se cambió nada.")
    }

    @Test
    fun `a malformed row tells the teacher to fix the file`() = runTest {
        reader.malformedRow = 15

        val state: ImportPreviewUiState = viewModel().state.value

        assertThat(state.rejection?.instruction)
            .isEqualTo("Corrige el archivo y vuelve a intentarlo. No se cambió nada.")
    }

    @Test
    fun `a template of another grade is rejected with both grades`() = runTest {
        reader.roster = SiagieRoster(
            gradeNumber = 3,
            sectionName = null,
            students = listOf(entry(FIRST_CODE, "ALVARADO QUISPE, MARIA")),
        )

        val state: ImportPreviewUiState = viewModel().state.value

        assertThat(state.rejection?.expected).isEqualTo("6to A")
        assertThat(state.rejection?.found).isEqualTo("3ro")
        assertThat(state.rejection?.foundLabel).isEqualTo("Grado en el archivo")
        assertThat(state.rejection?.instruction)
            .isEqualTo("Elige otro archivo o abre la sección correcta. No se cambió nada.")
        assertThat(state.canApply).isFalse()
    }

    @Test
    fun `a template of another section is rejected with both section names`() = runTest {
        reader.roster = SiagieRoster(
            gradeNumber = 6,
            sectionName = "B",
            students = listOf(entry(FIRST_CODE, "ALVARADO QUISPE, MARIA")),
        )

        val state: ImportPreviewUiState = viewModel().state.value

        assertThat(state.rejection?.expected).isEqualTo("6to A")
        assertThat(state.rejection?.found).isEqualTo("B")
        assertThat(state.rejection?.foundLabel).isEqualTo("Archivo")
        assertThat(state.rejection?.instruction)
            .isEqualTo("Elige otro archivo o abre la sección correcta. No se cambió nada.")
    }

    @Test
    fun `applying the import stores the students and leaves the screen`() = runTest {
        reader.roster = rosterOf(entry(FIRST_CODE, "ALVARADO QUISPE, MARIA"))
        val viewModel: ImportPreviewViewModel = viewModel()

        viewModel.effects.test {
            viewModel.onIntent(ImportPreviewUiIntent.ApplyClicked)

            assertThat(awaitItem()).isInstanceOf(ImportPreviewUiEffect.ShowMessage::class.java)
            assertThat(awaitItem()).isEqualTo(ImportPreviewUiEffect.NavigateBack)
        }
        assertThat(students.listBySection(sectionId).map { it.fullName })
            .containsExactly("ALVARADO QUISPE, MARIA")
    }

    @Test
    fun `an unselected withdrawal keeps the student in the section`() = runTest {
        students.students.value = listOf(enrolled("student-1", SECOND_CODE, "BAUTISTA HUAMAN, JOSE"))
        reader.roster = rosterOf(entry(FIRST_CODE, "ALVARADO QUISPE, MARIA"))
        val viewModel: ImportPreviewViewModel = viewModel()

        viewModel.onIntent(ImportPreviewUiIntent.WithdrawalToggled(StudentId("student-1"), isSelected = false))
        viewModel.onIntent(ImportPreviewUiIntent.ApplyClicked)

        assertThat(requireNotNull(students.findById(StudentId("student-1"))).withdrawalDate).isNull()
    }

    @Test
    fun `a selected withdrawal is dated with today`() = runTest {
        students.students.value = listOf(enrolled("student-1", SECOND_CODE, "BAUTISTA HUAMAN, JOSE"))
        reader.roster = rosterOf(entry(FIRST_CODE, "ALVARADO QUISPE, MARIA"))
        val viewModel: ImportPreviewViewModel = viewModel()

        viewModel.onIntent(ImportPreviewUiIntent.ApplyClicked)

        assertThat(requireNotNull(students.findById(StudentId("student-1"))).withdrawalDate)
            .isEqualTo(LocalDate.of(2026, 9, 10))
    }

    @Test
    fun `a group opens and closes`() = runTest {
        reader.roster = rosterOf(entry(FIRST_CODE, "ALVARADO QUISPE, MARIA"))
        val viewModel: ImportPreviewViewModel = viewModel()

        viewModel.onIntent(ImportPreviewUiIntent.GroupToggled(ImportGroup.CREATED))
        assertThat(viewModel.state.value.expandedGroup).isEqualTo(ImportGroup.CREATED)

        viewModel.onIntent(ImportPreviewUiIntent.GroupToggled(ImportGroup.CREATED))
        assertThat(viewModel.state.value.expandedGroup).isNull()
    }

    private fun viewModel(): ImportPreviewViewModel {
        val planner = SiagieImportPlanner(sections, students, documents, reader)
        return ImportPreviewViewModel(
            sectionId = sectionId,
            uri = URI,
            getSection = GetSectionUseCase(sections),
            previewImport = PreviewSiagieImportUseCase(planner),
            applyImport = ApplySiagieImportUseCase(
                planner = planner,
                students = students,
                store = store,
                idGenerator = IdGenerator { "student-imported" },
                clock = clock,
            ),
            clock = clock,
        )
    }

    private fun rosterOf(vararg entries: SiagieRosterStudent): SiagieRoster =
        SiagieRoster(gradeNumber = 6, sectionName = null, students = entries.toList())

    private fun entry(code: String, fullName: String): SiagieRosterStudent =
        SiagieRosterStudent(siagieId = "1001", code = StudentCode(code), fullName = fullName)

    private fun enrolled(id: String, code: String, fullName: String): Student = Student(
        id = StudentId(id),
        sectionId = sectionId,
        code = StudentCode(code),
        fullName = fullName,
    )
}
