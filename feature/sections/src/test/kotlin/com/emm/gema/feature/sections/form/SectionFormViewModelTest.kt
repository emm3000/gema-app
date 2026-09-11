package com.emm.gema.feature.sections.form

import app.cash.turbine.test
import com.emm.gema.core.domain.attendance.AttendanceRecord
import com.emm.gema.core.domain.attendance.AttendanceStatus
import com.emm.gema.core.domain.attendance.CountAttendanceDaysUseCase
import com.emm.gema.core.domain.id.IdGenerator
import com.emm.gema.core.domain.evaluation.GetPeriodLevelCountUseCase
import com.emm.gema.core.domain.section.CreateSectionUseCase
import com.emm.gema.core.domain.section.DeleteSectionUseCase
import com.emm.gema.core.domain.section.GetSectionUseCase
import com.emm.gema.core.domain.section.Grade
import com.emm.gema.core.domain.section.Section
import com.emm.gema.core.domain.student.Student
import com.emm.gema.core.domain.student.StudentCode
import com.emm.gema.core.domain.section.UpdateSectionUseCase
import com.emm.gema.core.domain.student.GetStudentsUseCase
import com.emm.gema.feature.sections.FakeActivityRepository
import com.emm.gema.feature.sections.FakeAttendanceRepository
import com.emm.gema.feature.sections.FakeEvidenceLevelRepository
import com.emm.gema.feature.sections.FakePeriodLevelRepository
import com.emm.gema.feature.sections.FakeSectionAreaRepository
import com.emm.gema.feature.sections.FakeSiagieImportStore
import com.emm.gema.feature.sections.FakeWorkedCompetencyRepository
import com.emm.gema.feature.sections.FakeSectionRepository
import com.emm.gema.feature.sections.FakeStudentRepository
import com.emm.gema.feature.sections.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class SectionFormViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val existing = Section("section-1", "2026", Grade.THIRD, "A")
    private val sectionRepository = FakeSectionRepository(listOf(existing))
    private val sectionAreaRepository = FakeSectionAreaRepository()
    private val studentRepository = FakeStudentRepository()
    private val periodLevelRepository = FakePeriodLevelRepository()
    private val attendanceRepository = FakeAttendanceRepository()

    private fun viewModelFor(sectionId: String?): SectionFormViewModel = SectionFormViewModel(
        schoolYearId = "2026",
        sectionId = sectionId,
        getSection = GetSectionUseCase(sectionRepository),
        createSection = CreateSectionUseCase(sectionRepository, IdGenerator { "section-2" }),
        updateSection = UpdateSectionUseCase(sectionRepository),
        deleteSection = DeleteSectionUseCase(
            sectionRepository,
            sectionAreaRepository,
            FakeWorkedCompetencyRepository(),
            studentRepository,
            FakeSiagieImportStore(),
            periodLevelRepository,
            attendanceRepository,
            FakeActivityRepository(),
            FakeEvidenceLevelRepository(),
        ),
        getStudents = GetStudentsUseCase(studentRepository),
        getPeriodLevelCount = GetPeriodLevelCountUseCase(periodLevelRepository),
        countAttendanceDays = CountAttendanceDaysUseCase(attendanceRepository),
    )

    @Test
    fun `a new section starts empty and cannot be deleted`() {
        val state: SectionFormUiState = viewModelFor(null).state.value

        assertThat(state.isLoading).isFalse()
        assertThat(state.sectionId).isNull()
        assertThat(state.canSave).isFalse()
        assertThat(state.canDelete).isFalse()
    }

    @Test
    fun `an existing section is loaded into the form`() {
        val state: SectionFormUiState = viewModelFor(existing.id).state.value

        assertThat(state.grade).isEqualTo(Grade.THIRD)
        assertThat(state.sectionName).isEqualTo("A")
        assertThat(state.canSave).isTrue()
        assertThat(state.canDelete).isTrue()
    }

    @Test
    fun `saving a new section adds it to the school year`() = runTest {
        val viewModel: SectionFormViewModel = viewModelFor(null)
        viewModel.onIntent(SectionFormUiIntent.GradeSelected(Grade.FIRST))
        viewModel.onIntent(SectionFormUiIntent.SectionNameChanged("B"))

        viewModel.effects.test {
            viewModel.onIntent(SectionFormUiIntent.SaveClicked)

            assertThat(awaitItem()).isEqualTo(SectionFormUiEffect.NavigateBack)
        }
        assertThat(sectionRepository.sections.value.map { it.name }).containsExactly("A", "B")
    }

    @Test
    fun `saving an existing section renames it instead of adding one`() = runTest {
        val viewModel: SectionFormViewModel = viewModelFor(existing.id)
        viewModel.onIntent(SectionFormUiIntent.SectionNameChanged("Unica"))

        viewModel.effects.test {
            viewModel.onIntent(SectionFormUiIntent.SaveClicked)

            assertThat(awaitItem()).isEqualTo(SectionFormUiEffect.NavigateBack)
        }
        assertThat(sectionRepository.sections.value.single().name).isEqualTo("Unica")
    }

    @Test
    fun `a blank name is reported and blocks saving`() {
        val viewModel: SectionFormViewModel = viewModelFor(existing.id)

        viewModel.onIntent(SectionFormUiIntent.SectionNameChanged(" "))

        assertThat(viewModel.state.value.sectionNameError).isNotNull()
        assertThat(viewModel.state.value.canSave).isFalse()
    }

    @Test
    fun `deleting asks for confirmation before removing anything`() = runTest {
        val viewModel: SectionFormViewModel = viewModelFor(existing.id)

        viewModel.onIntent(SectionFormUiIntent.DeleteClicked)

        assertThat(viewModel.state.value.deleteConfirmation).isNotNull()
        assertThat(sectionRepository.sections.value).isNotEmpty()

        viewModel.effects.test {
            viewModel.onIntent(SectionFormUiIntent.DeleteConfirmed)

            assertThat(awaitItem()).isEqualTo(SectionFormUiEffect.NavigateBack)
        }
        assertThat(sectionRepository.sections.value).isEmpty()
    }

    @Test
    fun `the confirmation counts the students that would be lost`() = runTest {
        studentRepository.students.value = listOf(
            Student("student-1", existing.id, StudentCode("12345678901234"), "ACOSTA RIVERA, Luz Maria"),
            Student("student-2", existing.id, StudentCode("12345678901235"), "BAUTISTA QUISPE, Jose"),
        )
        attendanceRepository.record(
            AttendanceRecord(existing.id, "student-1", LocalDate.of(2026, 9, 10), AttendanceStatus.ABSENT),
        )
        val viewModel: SectionFormViewModel = viewModelFor(existing.id)

        viewModel.onIntent(SectionFormUiIntent.DeleteClicked)

        assertThat(viewModel.state.value.deleteConfirmation?.studentCount).isEqualTo(2)
        assertThat(viewModel.state.value.deleteConfirmation?.attendanceDayCount).isEqualTo(1)
    }

    @Test
    fun `dismissing the confirmation keeps the section`() {
        val viewModel: SectionFormViewModel = viewModelFor(existing.id)

        viewModel.onIntent(SectionFormUiIntent.DeleteClicked)
        viewModel.onIntent(SectionFormUiIntent.DeleteDismissed)

        assertThat(viewModel.state.value.deleteConfirmation).isNull()
        assertThat(sectionRepository.sections.value).isNotEmpty()
    }

    @Test
    fun `a new section cannot be deleted`() = runTest {
        val viewModel: SectionFormViewModel = viewModelFor(null)

        viewModel.effects.test {
            viewModel.onIntent(SectionFormUiIntent.DeleteConfirmed)

            expectNoEvents()
        }
        assertThat(sectionRepository.sections.value).isNotEmpty()
    }

    @Test
    fun `a failing save is reported`() = runTest {
        val viewModel: SectionFormViewModel = viewModelFor(existing.id)
        sectionRepository.failsOnce = true

        viewModel.effects.test {
            viewModel.onIntent(SectionFormUiIntent.SaveClicked)

            assertThat(awaitItem()).isInstanceOf(SectionFormUiEffect.ShowMessage::class.java)
        }
    }
}
