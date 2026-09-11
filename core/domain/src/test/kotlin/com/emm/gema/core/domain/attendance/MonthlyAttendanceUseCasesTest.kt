package com.emm.gema.core.domain.attendance

import com.emm.gema.core.domain.fake.InMemoryAttendanceRepository
import com.emm.gema.core.domain.fake.InMemoryStudentRepository
import com.emm.gema.core.domain.siagie.AttendanceExportEntry
import com.emm.gema.core.domain.siagie.AttendanceExportFile
import com.emm.gema.core.domain.siagie.MonthlyAttendanceExporter
import com.emm.gema.core.domain.student.Student
import com.emm.gema.core.domain.student.StudentCode
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

private const val SECTION_ID: String = "section-1"
private val september: YearMonth = YearMonth.of(2026, 9)

class MonthlyAttendanceUseCasesTest {

    private val luz: Student = student("student-1", "12345678901234", "ACOSTA RIVERA, Luz Maria")
    private val jose: Student = student("student-2", "12345678901235", "BAUTISTA QUISPE, Jose")

    private val studentRepository = InMemoryStudentRepository(listOf(jose, luz))
    private val attendanceRepository = InMemoryAttendanceRepository()

    private val getMonthlySummary = GetMonthlyAttendanceSummaryUseCase(studentRepository, attendanceRepository)

    @Test
    fun `counts land per status per student and days are counted once`() = runTest {
        attendanceRepository.record(record(luz.id, september.atDay(1), AttendanceStatus.PRESENT))
        attendanceRepository.record(record(luz.id, september.atDay(2), AttendanceStatus.LATE))
        attendanceRepository.record(record(jose.id, september.atDay(1), AttendanceStatus.ABSENT))
        attendanceRepository.record(record(jose.id, september.atDay(2), AttendanceStatus.JUSTIFIED))

        val summary: MonthlyAttendanceSummary = getMonthlySummary(SECTION_ID, september).first()

        assertThat(summary.recordedDayCount).isEqualTo(2)
        val luzCounts: StudentAttendanceMonthCount = summary.rows.single { it.studentId == luz.id }
        assertThat(luzCounts.presentCount).isEqualTo(1)
        assertThat(luzCounts.lateCount).isEqualTo(1)
        val joseCounts: StudentAttendanceMonthCount = summary.rows.single { it.studentId == jose.id }
        assertThat(joseCounts.absentCount).isEqualTo(1)
        assertThat(joseCounts.justifiedCount).isEqualTo(1)
    }

    @Test
    fun `an unmarked day contributes no count to any status`() = runTest {
        val summary: MonthlyAttendanceSummary = getMonthlySummary(SECTION_ID, september).first()

        assertThat(summary.recordedDayCount).isEqualTo(0)
        assertThat(summary.rows.map { it.presentCount + it.lateCount + it.absentCount + it.justifiedCount })
            .containsExactly(0, 0)
    }

    @Test
    fun `rows are ordered by surname like every other roster`() = runTest {
        val summary: MonthlyAttendanceSummary = getMonthlySummary(SECTION_ID, september).first()

        assertThat(summary.rows.map { it.displayName })
            .containsExactly("ACOSTA RIVERA, Luz Maria", "BAUTISTA QUISPE, Jose").inOrder()
    }

    @Test
    fun `a student withdrawn before the month is left out of the summary`() = runTest {
        studentRepository.save(jose.copy(withdrawalDate = september.atDay(1).minusDays(1)))

        val summary: MonthlyAttendanceSummary = getMonthlySummary(SECTION_ID, september).first()

        assertThat(summary.rows.map { it.studentId }).containsExactly(luz.id)
    }

    @Test
    fun `a different month keeps its own records apart`() = runTest {
        attendanceRepository.record(record(luz.id, september.atDay(1), AttendanceStatus.ABSENT))
        attendanceRepository.record(record(luz.id, september.minusMonths(1).atDay(1), AttendanceStatus.LATE))

        val summary: MonthlyAttendanceSummary = getMonthlySummary(SECTION_ID, september).first()

        assertThat(summary.rows.single { it.studentId == luz.id }.absentCount).isEqualTo(1)
        assertThat(summary.rows.single { it.studentId == luz.id }.lateCount).isEqualTo(0)
    }

    @Test
    fun `export builds one entry per student with its dated statuses and hands them to the writer`() = runTest {
        attendanceRepository.record(record(luz.id, september.atDay(1), AttendanceStatus.PRESENT))
        attendanceRepository.record(record(jose.id, september.atDay(1), AttendanceStatus.ABSENT))
        val exporter = RecordingMonthlyAttendanceExporter()
        val exportMonthlyAttendance = ExportMonthlyAttendanceUseCase(studentRepository, attendanceRepository, exporter)

        val file: AttendanceExportFile = exportMonthlyAttendance(SECTION_ID, september, "content://attendance.xlsx")

        assertThat(file).isEqualTo(AttendanceExportFile("exported.xlsx", "/tmp/exported.xlsx"))
        assertThat(exporter.receivedUri).isEqualTo("content://attendance.xlsx")
        assertThat(exporter.receivedMonth).isEqualTo(september)
        val luzEntry: AttendanceExportEntry = exporter.receivedEntries.single { it.studentCode == luz.code }
        assertThat(luzEntry.statusesByDate).containsEntry(september.atDay(1), AttendanceStatus.PRESENT)
        val joseEntry: AttendanceExportEntry = exporter.receivedEntries.single { it.studentCode == jose.code }
        assertThat(joseEntry.statusesByDate).containsEntry(september.atDay(1), AttendanceStatus.ABSENT)
    }

    private fun record(studentId: String, date: LocalDate, status: AttendanceStatus): AttendanceRecord =
        AttendanceRecord(sectionId = SECTION_ID, studentId = studentId, date = date, status = status)

    private fun student(id: String, code: String, fullName: String): Student = Student(
        id = id,
        sectionId = SECTION_ID,
        code = StudentCode(code),
        fullName = fullName,
    )
}

private class RecordingMonthlyAttendanceExporter : MonthlyAttendanceExporter {

    var receivedUri: String? = null
    var receivedMonth: YearMonth? = null
    var receivedEntries: List<AttendanceExportEntry> = emptyList()

    override suspend fun export(
        templateUri: String,
        month: YearMonth,
        entries: List<AttendanceExportEntry>,
    ): AttendanceExportFile {
        receivedUri = templateUri
        receivedMonth = month
        receivedEntries = entries
        return AttendanceExportFile("exported.xlsx", "/tmp/exported.xlsx")
    }
}
