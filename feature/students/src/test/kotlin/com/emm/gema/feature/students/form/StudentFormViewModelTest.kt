package com.emm.gema.feature.students.form

import app.cash.turbine.test
import com.emm.gema.core.domain.id.IdGenerator
import com.emm.gema.core.domain.section.SectionId
import com.emm.gema.core.domain.student.GetStudentUseCase
import com.emm.gema.core.domain.student.ReactivateStudentUseCase
import com.emm.gema.core.domain.student.SaveStudentUseCase
import com.emm.gema.core.domain.student.Student
import com.emm.gema.core.domain.student.StudentCode
import com.emm.gema.core.domain.student.StudentId
import com.emm.gema.core.domain.student.WithdrawStudentUseCase
import com.emm.gema.feature.students.FakeStudentRepository
import com.emm.gema.feature.students.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneOffset
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

private val sectionId: SectionId = SectionId("section-1")
private const val FIRST_CODE: String = "12345678901234"
private const val SECOND_CODE: String = "12345678901235"

class StudentFormViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val today = LocalDate.of(2026, 9, 10)
    private val clock: Clock = Clock.fixed(today.atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneOffset.UTC)
    private val luz = Student(StudentId("student-1"), sectionId, StudentCode(FIRST_CODE), "ACOSTA RIVERA, Luz Maria")
    private val repository = FakeStudentRepository(listOf(luz))

    private fun viewModelFor(studentId: StudentId?): StudentFormViewModel = StudentFormViewModel(
        sectionId = sectionId,
        studentId = studentId,
        getStudent = GetStudentUseCase(repository),
        saveStudent = SaveStudentUseCase(repository, IdGenerator { "student-2" }),
        withdrawStudent = WithdrawStudentUseCase(repository),
        reactivateStudent = ReactivateStudentUseCase(repository),
        clock = clock,
    )

    @Test
    fun `a new student starts empty and cannot be saved`() {
        val state: StudentFormUiState = viewModelFor(null).state.value

        assertThat(state.isLoading).isFalse()
        assertThat(state.studentId).isNull()
        assertThat(state.canSave).isFalse()
        assertThat(state.studentCodeError).isNull()
    }

    @Test
    fun `an existing student is loaded into the form`() {
        val state: StudentFormUiState = viewModelFor(luz.id).state.value

        assertThat(state.studentCode).isEqualTo(FIRST_CODE)
        assertThat(state.fullName).isEqualTo("ACOSTA RIVERA, Luz Maria")
        assertThat(state.canSave).isTrue()
        assertThat(state.isWithdrawn).isFalse()
    }

    @Test
    fun `the code keeps only digits and never grows past fourteen`() {
        val viewModel: StudentFormViewModel = viewModelFor(null)

        viewModel.onIntent(StudentFormUiIntent.StudentCodeChanged("12a34-5678 90123456"))

        assertThat(viewModel.state.value.studentCode).isEqualTo("12345678901234")
        assertThat(viewModel.state.value.studentCodeHint).isEqualTo("14 de 14 dígitos")
    }

    @Test
    fun `a short code is reported and blocks saving`() {
        val viewModel: StudentFormViewModel = viewModelFor(null)

        viewModel.onIntent(StudentFormUiIntent.StudentCodeChanged("123"))
        viewModel.onIntent(StudentFormUiIntent.FullNameChanged("BAUTISTA QUISPE, Jose"))

        assertThat(viewModel.state.value.studentCodeError).isNotNull()
        assertThat(viewModel.state.value.canSave).isFalse()
    }

    @Test
    fun `a blank name is reported and blocks saving`() {
        val viewModel: StudentFormViewModel = viewModelFor(luz.id)

        viewModel.onIntent(StudentFormUiIntent.FullNameChanged(" "))

        assertThat(viewModel.state.value.fullNameError).isNotNull()
        assertThat(viewModel.state.value.canSave).isFalse()
    }

    @Test
    fun `saving a new student adds it to the section`() = runTest {
        val viewModel: StudentFormViewModel = viewModelFor(null)
        viewModel.onIntent(StudentFormUiIntent.StudentCodeChanged(SECOND_CODE))
        viewModel.onIntent(StudentFormUiIntent.FullNameChanged("BAUTISTA QUISPE, Jose"))

        viewModel.effects.test {
            viewModel.onIntent(StudentFormUiIntent.SaveClicked)

            assertThat(awaitItem()).isEqualTo(StudentFormUiEffect.NavigateBack)
        }
        assertThat(repository.students.value.map { it.fullName })
            .containsExactly("ACOSTA RIVERA, Luz Maria", "BAUTISTA QUISPE, Jose")
    }

    @Test
    fun `saving an existing student renames it instead of adding one`() = runTest {
        val viewModel: StudentFormViewModel = viewModelFor(luz.id)
        viewModel.onIntent(StudentFormUiIntent.FullNameChanged("ACOSTA RIVERA, Luz"))

        viewModel.effects.test {
            viewModel.onIntent(StudentFormUiIntent.SaveClicked)

            assertThat(awaitItem()).isEqualTo(StudentFormUiEffect.NavigateBack)
        }
        assertThat(repository.students.value.single().fullName).isEqualTo("ACOSTA RIVERA, Luz")
    }

    @Test
    fun `a code already used in the section is reported on the field`() = runTest {
        val viewModel: StudentFormViewModel = viewModelFor(null)
        viewModel.onIntent(StudentFormUiIntent.StudentCodeChanged(FIRST_CODE))
        viewModel.onIntent(StudentFormUiIntent.FullNameChanged("BAUTISTA QUISPE, Jose"))

        viewModel.effects.test {
            viewModel.onIntent(StudentFormUiIntent.SaveClicked)

            expectNoEvents()
        }
        assertThat(viewModel.state.value.studentCodeError).isNotNull()
        assertThat(viewModel.state.value.canSave).isFalse()
        assertThat(repository.students.value).hasSize(1)
    }

    @Test
    fun `marking the student as withdrawn dates the withdrawal today`() {
        val viewModel: StudentFormViewModel = viewModelFor(luz.id)

        viewModel.onIntent(StudentFormUiIntent.WithdrawnToggled(true))

        assertThat(viewModel.state.value.withdrawalDate).isEqualTo(today)
        assertThat(viewModel.state.value.canSave).isTrue()
    }

    @Test
    fun `saving a withdrawn student keeps it with its withdrawal date`() = runTest {
        val viewModel: StudentFormViewModel = viewModelFor(luz.id)
        viewModel.onIntent(StudentFormUiIntent.WithdrawnToggled(true))
        viewModel.onIntent(StudentFormUiIntent.WithdrawalDateChanged(LocalDate.of(2026, 9, 4)))

        viewModel.effects.test {
            viewModel.onIntent(StudentFormUiIntent.SaveClicked)

            assertThat(awaitItem()).isEqualTo(StudentFormUiEffect.NavigateBack)
        }
        val stored: Student = repository.students.value.single()
        assertThat(stored.withdrawalDate).isEqualTo(LocalDate.of(2026, 9, 4))
        assertThat(stored.fullName).isEqualTo("ACOSTA RIVERA, Luz Maria")
    }

    @Test
    fun `turning the switch off reactivates the student`() = runTest {
        repository.save(luz.copy(withdrawalDate = LocalDate.of(2026, 9, 4)))
        val viewModel: StudentFormViewModel = viewModelFor(luz.id)
        assertThat(viewModel.state.value.isWithdrawn).isTrue()

        viewModel.onIntent(StudentFormUiIntent.WithdrawnToggled(false))
        viewModel.effects.test {
            viewModel.onIntent(StudentFormUiIntent.SaveClicked)

            assertThat(awaitItem()).isEqualTo(StudentFormUiEffect.NavigateBack)
        }
        assertThat(repository.students.value.single().withdrawalDate).isNull()
    }

    @Test
    fun `a failing save is reported`() = runTest {
        val viewModel: StudentFormViewModel = viewModelFor(luz.id)
        repository.failsOnce = true

        viewModel.effects.test {
            viewModel.onIntent(StudentFormUiIntent.SaveClicked)

            assertThat(awaitItem()).isInstanceOf(StudentFormUiEffect.ShowMessage::class.java)
        }
    }
}
