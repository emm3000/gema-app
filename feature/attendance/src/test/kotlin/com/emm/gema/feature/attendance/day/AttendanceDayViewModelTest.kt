package com.emm.gema.feature.attendance.day

import app.cash.turbine.test
import com.emm.gema.core.domain.attendance.AttendanceStatus
import com.emm.gema.core.domain.attendance.GetAttendanceDayUseCase
import com.emm.gema.core.domain.attendance.RecordAttendanceUseCase
import com.emm.gema.core.domain.section.GetSectionUseCase
import com.emm.gema.core.domain.section.Grade
import com.emm.gema.core.domain.section.Section
import com.emm.gema.core.domain.student.Student
import com.emm.gema.core.domain.student.StudentCode
import com.emm.gema.feature.attendance.FakeAttendanceRepository
import com.emm.gema.feature.attendance.FakeSectionRepository
import com.emm.gema.feature.attendance.FakeStudentRepository
import com.emm.gema.feature.attendance.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneOffset

private const val sectionId: String = "section-1"
private val today: LocalDate = LocalDate.of(2026, 9, 10)
private val yesterday: LocalDate = today.minusDays(1)

class AttendanceDayViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val section = Section(sectionId, "2026", Grade.THIRD, "A")
    private val luz = student("student-1", "12345678901234", "ACOSTA RIVERA, Luz Maria")
    private val jose = student("student-2", "12345678901235", "BAUTISTA QUISPE, Jose")

    private val sectionRepository = FakeSectionRepository(listOf(section))
    private val studentRepository = FakeStudentRepository(listOf(jose, luz))
    private val attendanceRepository = FakeAttendanceRepository()
    private val clock: Clock = Clock.fixed(today.atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneOffset.UTC)

    private fun viewModel(initialDate: LocalDate? = null): AttendanceDayViewModel = AttendanceDayViewModel(
        sectionId = sectionId,
        initialDate = initialDate,
        getSection = GetSectionUseCase(sectionRepository),
        getAttendanceDay = GetAttendanceDayUseCase(studentRepository, attendanceRepository),
        recordAttendance = RecordAttendanceUseCase(attendanceRepository, clock),
        clock = clock,
    )

    @Test
    fun `today opens on every student present and nothing recorded`() {
        val state: AttendanceDayUiState = viewModel().state.value

        assertThat(state.isLoading).isFalse()
        assertThat(state.sectionTitle).isEqualTo("3° A")
        assertThat(state.date).isEqualTo(today)
        assertThat(state.dateLabel).isEqualTo("Jue 10 set 2026")
        assertThat(state.rows.map { it.displayName })
            .containsExactly("ACOSTA RIVERA, Luz Maria", "BAUTISTA QUISPE, Jose").inOrder()
        assertThat(state.rows.map { it.isRecorded }).containsExactly(false, false)
        assertThat(state.presentCount).isEqualTo(2)
        assertThat(state.totalCount).isEqualTo(2)
        assertThat(state.unmarkedCount).isEqualTo(2)
        assertThat(attendanceRepository.records.value).isEmpty()
    }

    @Test
    fun `a tap records one student and updates the counters`() = runTest {
        val viewModel: AttendanceDayViewModel = viewModel()

        viewModel.onIntent(AttendanceDayUiIntent.StatusSelected(luz.id, AttendanceStatus.ABSENT))

        val state: AttendanceDayUiState = viewModel.state.value
        assertThat(state.rows.single { it.studentId == luz.id }.status).isEqualTo(AttendanceStatus.ABSENT)
        assertThat(state.rows.single { it.studentId == luz.id }.isRecorded).isTrue()
        assertThat(state.presentCount).isEqualTo(1)
        assertThat(state.unmarkedCount).isEqualTo(1)
        assertThat(attendanceRepository.records.value).hasSize(1)
    }

    @Test
    fun `marking everyone present only records the students still unmarked`() = runTest {
        val viewModel: AttendanceDayViewModel = viewModel()
        viewModel.onIntent(AttendanceDayUiIntent.StatusSelected(luz.id, AttendanceStatus.LATE))

        viewModel.onIntent(AttendanceDayUiIntent.MarkAllPresent)

        val state: AttendanceDayUiState = viewModel.state.value
        assertThat(state.rows.single { it.studentId == luz.id }.status).isEqualTo(AttendanceStatus.LATE)
        assertThat(state.rows.single { it.studentId == jose.id }.status).isEqualTo(AttendanceStatus.PRESENT)
        assertThat(state.rows.map { it.isRecorded }).containsExactly(true, true)
        assertThat(state.unmarkedCount).isEqualTo(0)
        assertThat(state.canMarkAllPresent).isFalse()
    }

    @Test
    fun `the day stepper walks back and cannot pass today`() {
        val viewModel: AttendanceDayViewModel = viewModel()

        assertThat(viewModel.state.value.canGoForward).isFalse()

        viewModel.onIntent(AttendanceDayUiIntent.PreviousDayClicked)

        assertThat(viewModel.state.value.date).isEqualTo(yesterday)
        assertThat(viewModel.state.value.canGoForward).isTrue()

        viewModel.onIntent(AttendanceDayUiIntent.NextDayClicked)

        assertThat(viewModel.state.value.date).isEqualTo(today)

        viewModel.onIntent(AttendanceDayUiIntent.NextDayClicked)

        assertThat(viewModel.state.value.date).isEqualTo(today)
    }

    @Test
    fun `a past date shows its own records`() = runTest {
        val viewModel: AttendanceDayViewModel = viewModel(initialDate = yesterday)

        viewModel.onIntent(AttendanceDayUiIntent.StatusSelected(luz.id, AttendanceStatus.JUSTIFIED))
        viewModel.onIntent(AttendanceDayUiIntent.NextDayClicked)

        assertThat(viewModel.state.value.rows.single { it.studentId == luz.id }.isRecorded).isFalse()

        viewModel.onIntent(AttendanceDayUiIntent.PreviousDayClicked)

        assertThat(viewModel.state.value.rows.single { it.studentId == luz.id }.status)
            .isEqualTo(AttendanceStatus.JUSTIFIED)
    }

    @Test
    fun `a picked future date is refused and reported`() = runTest {
        val viewModel: AttendanceDayViewModel = viewModel()

        viewModel.effects.test {
            viewModel.onIntent(AttendanceDayUiIntent.DatePicked(today.plusDays(1)))

            assertThat(awaitItem()).isInstanceOf(AttendanceDayUiEffect.ShowMessage::class.java)
        }
        assertThat(viewModel.state.value.date).isEqualTo(today)
    }

    @Test
    fun `a withdrawn student is not listed from the withdrawal date on`() = runTest {
        studentRepository.save(jose.copy(withdrawalDate = today))
        val viewModel: AttendanceDayViewModel = viewModel()

        assertThat(viewModel.state.value.rows.map { it.studentId }).containsExactly(luz.id)

        viewModel.onIntent(AttendanceDayUiIntent.PreviousDayClicked)

        assertThat(viewModel.state.value.rows.map { it.studentId }).containsExactly(luz.id, jose.id)
    }

    @Test
    fun `a failing write is reported`() = runTest {
        val viewModel: AttendanceDayViewModel = viewModel()
        attendanceRepository.failsOnce = true

        viewModel.effects.test {
            viewModel.onIntent(AttendanceDayUiIntent.StatusSelected(luz.id, AttendanceStatus.ABSENT))

            assertThat(awaitItem()).isInstanceOf(AttendanceDayUiEffect.ShowMessage::class.java)
        }
    }

    @Test
    fun `going back leaves the screen`() = runTest {
        val viewModel: AttendanceDayViewModel = viewModel()

        viewModel.effects.test {
            viewModel.onIntent(AttendanceDayUiIntent.BackClicked)

            assertThat(awaitItem()).isEqualTo(AttendanceDayUiEffect.NavigateBack)
        }
    }
}

private fun student(id: String, code: String, fullName: String): Student = Student(
    id = id,
    sectionId = sectionId,
    code = StudentCode(code),
    fullName = fullName,
)
