package com.emm.gema.feature.students.list

import app.cash.turbine.test
import com.emm.gema.core.domain.section.GetSectionUseCase
import com.emm.gema.core.domain.section.Grade
import com.emm.gema.core.domain.section.Section
import com.emm.gema.core.domain.student.GetStudentsUseCase
import com.emm.gema.core.domain.student.ReactivateStudentUseCase
import com.emm.gema.core.domain.student.Student
import com.emm.gema.core.domain.student.StudentCode
import com.emm.gema.feature.students.FakeSectionRepository
import com.emm.gema.feature.students.FakeStudentRepository
import com.emm.gema.feature.students.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

private const val SECTION_ID: String = "section-1"

class StudentsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val section = Section(SECTION_ID, "2026", Grade.THIRD, "A")
    private val luz = student("student-1", "12345678901234", "ACOSTA RIVERA, Luz Maria")
    private val jose = student("student-2", "12345678901235", "BAUTISTA QUISPE, Jose")
    private val rosa = student(
        id = "student-3",
        code = "12345678901236",
        fullName = "CCAHUANA MAMANI, Rosa",
        withdrawalDate = LocalDate.of(2026, 9, 4),
    )

    private val studentRepository = FakeStudentRepository(listOf(rosa, jose, luz))
    private val sectionRepository = FakeSectionRepository(listOf(section))

    private fun viewModel(): StudentsViewModel = StudentsViewModel(
        sectionId = SECTION_ID,
        getSection = GetSectionUseCase(sectionRepository),
        getStudents = GetStudentsUseCase(studentRepository),
        reactivateStudent = ReactivateStudentUseCase(studentRepository),
    )

    @Test
    fun `the section title and the students are shown, withdrawn ones apart`() {
        val state: StudentsUiState = viewModel().state.value

        assertThat(state.isLoading).isFalse()
        assertThat(state.sectionTitle).isEqualTo("3° A")
        assertThat(state.activeStudents.map { it.displayName })
            .containsExactly("ACOSTA RIVERA, Luz Maria", "BAUTISTA QUISPE, Jose").inOrder()
        assertThat(state.withdrawnStudents.map { it.displayName }).containsExactly("CCAHUANA MAMANI, Rosa")
    }

    @Test
    fun `the query filters by name and by code`() {
        val viewModel: StudentsViewModel = viewModel()

        viewModel.onIntent(StudentsUiIntent.QueryChanged("bautista"))
        assertThat(viewModel.state.value.activeStudents.map { it.displayName })
            .containsExactly("BAUTISTA QUISPE, Jose")

        viewModel.onIntent(StudentsUiIntent.QueryChanged("12345678901234"))
        assertThat(viewModel.state.value.activeStudents.map { it.displayName })
            .containsExactly("ACOSTA RIVERA, Luz Maria")
    }

    @Test
    fun `an empty section is only empty while nothing is searched`() {
        val viewModel: StudentsViewModel = StudentsViewModel(
            sectionId = SECTION_ID,
            getSection = GetSectionUseCase(sectionRepository),
            getStudents = GetStudentsUseCase(FakeStudentRepository()),
            reactivateStudent = ReactivateStudentUseCase(studentRepository),
        )

        assertThat(viewModel.state.value.isEmpty).isTrue()

        viewModel.onIntent(StudentsUiIntent.QueryChanged("acosta"))

        assertThat(viewModel.state.value.isEmpty).isFalse()
    }

    @Test
    fun `the withdrawn section is collapsed until it is tapped`() {
        val viewModel: StudentsViewModel = viewModel()

        assertThat(viewModel.state.value.isWithdrawnExpanded).isFalse()

        viewModel.onIntent(StudentsUiIntent.WithdrawnSectionToggled)

        assertThat(viewModel.state.value.isWithdrawnExpanded).isTrue()
    }

    @Test
    fun `reactivating moves the student back to the active list`() = runTest {
        val viewModel: StudentsViewModel = viewModel()

        viewModel.onIntent(StudentsUiIntent.ReactivateClicked(rosa.id))

        assertThat(viewModel.state.value.withdrawnStudents).isEmpty()
        assertThat(viewModel.state.value.activeStudents.map { it.displayName })
            .contains("CCAHUANA MAMANI, Rosa")
    }

    @Test
    fun `a failing reactivation is reported`() = runTest {
        val viewModel: StudentsViewModel = viewModel()
        studentRepository.failsOnce = true

        viewModel.effects.test {
            viewModel.onIntent(StudentsUiIntent.ReactivateClicked(rosa.id))

            assertThat(awaitItem()).isInstanceOf(StudentsUiEffect.ShowMessage::class.java)
        }
    }

    @Test
    fun `adding and opening a student both reach the form`() = runTest {
        val viewModel: StudentsViewModel = viewModel()

        viewModel.effects.test {
            viewModel.onIntent(StudentsUiIntent.AddStudentClicked)
            assertThat(awaitItem()).isEqualTo(StudentsUiEffect.NavigateToStudentForm(SECTION_ID, null))

            viewModel.onIntent(StudentsUiIntent.StudentClicked(luz.id))
            assertThat(awaitItem()).isEqualTo(StudentsUiEffect.NavigateToStudentForm(SECTION_ID, luz.id))
        }
    }
}

private fun student(
    id: String,
    code: String,
    fullName: String,
    withdrawalDate: LocalDate? = null,
): Student = Student(
    id = id,
    sectionId = SECTION_ID,
    code = StudentCode(code),
    fullName = fullName,
    withdrawalDate = withdrawalDate,
)
