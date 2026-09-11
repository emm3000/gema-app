package com.emm.gema.feature.attendance.month

import app.cash.turbine.test
import com.emm.gema.core.domain.attendance.AttendanceRecord
import com.emm.gema.core.domain.attendance.AttendanceStatus
import com.emm.gema.core.domain.attendance.ExportMonthlyAttendanceUseCase
import com.emm.gema.core.domain.attendance.GetMonthlyAttendanceSummaryUseCase
import com.emm.gema.core.domain.schoolyear.SchoolYearId
import com.emm.gema.core.domain.section.GetSectionUseCase
import com.emm.gema.core.domain.section.Grade
import com.emm.gema.core.domain.section.Section
import com.emm.gema.core.domain.section.SectionId
import com.emm.gema.core.domain.siagie.AttendanceExportEntry
import com.emm.gema.core.domain.siagie.AttendanceExportFile
import com.emm.gema.core.domain.siagie.MonthlyAttendanceExporter
import com.emm.gema.core.domain.student.Student
import com.emm.gema.core.domain.student.StudentCode
import com.emm.gema.core.domain.student.StudentId
import com.emm.gema.feature.attendance.FakeAttendanceRepository
import com.emm.gema.feature.attendance.FakeSectionRepository
import com.emm.gema.feature.attendance.FakeStudentRepository
import com.emm.gema.feature.attendance.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import java.time.Clock
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneOffset
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

private val sectionId: SectionId = SectionId("section-1")
private val today: LocalDate = LocalDate.of(2026, 9, 10)
private val september: YearMonth = YearMonth.of(2026, 9)

class AttendanceMonthViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val section = Section(SectionId(sectionId.value), SchoolYearId("2026"), Grade.THIRD, "A")
    private val luz = student("student-1", "12345678901234", "ACOSTA RIVERA, Luz Maria")

    private val sectionRepository = FakeSectionRepository(listOf(section))
    private val studentRepository = FakeStudentRepository(listOf(luz))
    private val attendanceRepository = FakeAttendanceRepository()
    private val exporter = RecordingMonthlyAttendanceExporter()
    private val clock: Clock = Clock.fixed(today.atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneOffset.UTC)

    private fun viewModel(initialMonth: YearMonth? = null): AttendanceMonthViewModel = AttendanceMonthViewModel(
        sectionId = SectionId(sectionId.value),
        initialMonth = initialMonth,
        getSection = GetSectionUseCase(sectionRepository),
        getMonthlySummary = GetMonthlyAttendanceSummaryUseCase(studentRepository, attendanceRepository),
        exportMonthlyAttendance = ExportMonthlyAttendanceUseCase(studentRepository, attendanceRepository, exporter),
        clock = clock,
    )

    @Test
    fun `opens on the current month with export disabled until something is recorded`() {
        val state: AttendanceMonthUiState = viewModel().state.value

        assertThat(state.isLoading).isFalse()
        assertThat(state.sectionTitle).isEqualTo("3° A")
        assertThat(state.month).isEqualTo(september)
        assertThat(state.monthLabel).isEqualTo("setiembre 2026")
        assertThat(state.canExport).isFalse()
    }

    @Test
    fun `a recorded day turns counts and export on`() = runTest {
        attendanceRepository.records.value = listOf(
            AttendanceRecord(SectionId(sectionId.value), luz.id, september.atDay(1), AttendanceStatus.LATE),
        )
        val state: AttendanceMonthUiState = viewModel().state.value

        assertThat(state.recordedDayCount).isEqualTo(1)
        assertThat(state.canExport).isTrue()
        assertThat(state.rows.single().countsByStatus[AttendanceStatus.LATE]).isEqualTo(1)
    }

    @Test
    fun `stepping to the previous month reads that month's own records`() {
        attendanceRepository.records.value = listOf(
            AttendanceRecord(
                SectionId(sectionId.value),
                luz.id,
                september.minusMonths(1).atDay(5),
                AttendanceStatus.ABSENT,
            ),
        )
        val viewModel: AttendanceMonthViewModel = viewModel(initialMonth = september)

        viewModel.onIntent(AttendanceMonthUiIntent.PreviousMonthClicked)

        assertThat(viewModel.state.value.month).isEqualTo(september.minusMonths(1))
        assertThat(viewModel.state.value.monthLabel).isEqualTo("agosto 2026")
        assertThat(viewModel.state.value.rows.single().countsByStatus[AttendanceStatus.ABSENT]).isEqualTo(1)
    }

    @Test
    fun `export opens the document picker then shares the resulting file`() = runTest {
        attendanceRepository.records.value = listOf(
            AttendanceRecord(SectionId(sectionId.value), luz.id, september.atDay(1), AttendanceStatus.PRESENT),
        )
        val viewModel: AttendanceMonthViewModel = viewModel(initialMonth = september)

        viewModel.effects.test {
            viewModel.onIntent(AttendanceMonthUiIntent.ExportClicked)
            val opened = awaitItem() as AttendanceMonthUiEffect.OpenDocumentPicker
            assertThat(opened.mimeTypes).isNotEmpty()

            viewModel.onIntent(AttendanceMonthUiIntent.TemplatePicked("content://attendance.xlsx"))
            val shared = awaitItem() as AttendanceMonthUiEffect.ShareFile
            assertThat(shared.path).isEqualTo("/tmp/exported.xlsx")
        }
        assertThat(exporter.receivedUri).isEqualTo("content://attendance.xlsx")
        assertThat(exporter.receivedMonth).isEqualTo(september)
    }

    @Test
    fun `going back leaves the screen`() = runTest {
        val viewModel: AttendanceMonthViewModel = viewModel()

        viewModel.effects.test {
            viewModel.onIntent(AttendanceMonthUiIntent.BackClicked)

            assertThat(awaitItem()).isEqualTo(AttendanceMonthUiEffect.NavigateBack)
        }
    }

    private fun student(id: String, code: String, fullName: String): Student = Student(
        id = StudentId(id),
        sectionId = SectionId(sectionId.value),
        code = StudentCode(code),
        fullName = fullName,
    )
}

private class RecordingMonthlyAttendanceExporter : MonthlyAttendanceExporter {

    var receivedUri: String? = null
    var receivedMonth: YearMonth? = null

    override suspend fun export(
        templateUri: String,
        month: YearMonth,
        entries: List<AttendanceExportEntry>,
    ): AttendanceExportFile {
        receivedUri = templateUri
        receivedMonth = month
        return AttendanceExportFile("exported.xlsx", "/tmp/exported.xlsx")
    }
}
