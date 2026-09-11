package com.emm.gema.feature.sections.detail

import app.cash.turbine.test
import com.emm.gema.core.domain.section.GetSectionUseCase
import com.emm.gema.core.domain.section.Grade
import com.emm.gema.core.domain.section.Section
import com.emm.gema.core.domain.student.GetStudentsUseCase
import com.emm.gema.core.domain.student.Student
import com.emm.gema.core.domain.student.StudentCode
import com.emm.gema.feature.sections.FakeSectionRepository
import com.emm.gema.feature.sections.FakeStudentRepository
import com.emm.gema.feature.sections.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

private const val SECTION_ID: String = "section-1"

class SectionDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val section = Section(SECTION_ID, "2026", Grade.THIRD, "A")
    private val sectionRepository = FakeSectionRepository(listOf(section))
    private val studentRepository = FakeStudentRepository(
        listOf(
            student("student-1", "12345678901234"),
            student("student-2", "12345678901235", LocalDate.of(2026, 9, 4)),
        )
    )

    private fun viewModel(): SectionDetailViewModel = SectionDetailViewModel(
        sectionId = SECTION_ID,
        getSection = GetSectionUseCase(sectionRepository),
        getStudents = GetStudentsUseCase(studentRepository),
    )

    @Test
    fun `the hub shows the section and how many students still attend it`() {
        val state: SectionDetailUiState = viewModel().state.value

        assertThat(state.isLoading).isFalse()
        assertThat(state.sectionTitle).isEqualTo("3° A")
        assertThat(state.studentCount).isEqualTo(1)
    }

    @Test
    fun `the students row opens the student list of this section`() = runTest {
        val viewModel: SectionDetailViewModel = viewModel()

        viewModel.effects.test {
            viewModel.onIntent(SectionDetailUiIntent.StudentsClicked)

            assertThat(awaitItem()).isEqualTo(SectionDetailUiEffect.NavigateToStudents(SECTION_ID))
        }
    }

    @Test
    fun `the areas row opens the area selection of this section`() = runTest {
        val viewModel: SectionDetailViewModel = viewModel()

        viewModel.effects.test {
            viewModel.onIntent(SectionDetailUiIntent.AreasClicked)

            assertThat(awaitItem()).isEqualTo(SectionDetailUiEffect.NavigateToSectionAreas(SECTION_ID))
        }
    }

    @Test
    fun `renaming opens the form of this section inside its school year`() = runTest {
        val viewModel: SectionDetailViewModel = viewModel()

        viewModel.effects.test {
            viewModel.onIntent(SectionDetailUiIntent.RenameClicked)

            assertThat(awaitItem()).isEqualTo(SectionDetailUiEffect.NavigateToSectionForm("2026", SECTION_ID))
        }
    }
}

private fun student(id: String, code: String, withdrawalDate: LocalDate? = null): Student = Student(
    id = id,
    sectionId = SECTION_ID,
    code = StudentCode(code),
    fullName = "ACOSTA RIVERA, Luz Maria",
    withdrawalDate = withdrawalDate,
)
