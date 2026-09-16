package com.emm.gema.core.domain.attendance

import com.emm.gema.core.domain.fake.InMemoryAttendanceRepository
import com.emm.gema.core.domain.fake.InMemoryStudentRepository
import com.emm.gema.core.domain.siagie.AttendanceExportEntry
import com.emm.gema.core.domain.siagie.AttendanceExportFile
import com.emm.gema.core.domain.siagie.MonthlyAttendanceExporter
import com.emm.gema.core.domain.student.Student
import com.emm.gema.core.domain.student.StudentId
import com.google.common.truth.Truth.assertThat
import java.time.YearMonth
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ExportMonthlyAttendanceUseCaseTest {

    private val luz: Student = student("student-1", "12345678901234", "ACOSTA RIVERA, Luz Maria")
    private val jose: Student = student("student-2", "12345678901235", "BAUTISTA QUISPE, Jose")

    private val studentRepository = InMemoryStudentRepository(listOf(jose, luz))
    private val attendanceRepository = InMemoryAttendanceRepository()
    private val exporter = RecordingMonthlyAttendanceExporter()
    private val exportMonthlyAttendance = ExportMonthlyAttendanceUseCase(
        studentRepository,
        attendanceRepository,
        exporter,
    )

    @Test
    fun `export builds one entry per student with its dated statuses and hands them to the writer`() = runTest {
        attendanceRepository.record(record(luz.id, september.atDay(1), AttendanceStatus.PRESENT))
        attendanceRepository.record(record(jose.id, september.atDay(1), AttendanceStatus.ABSENT))

        val file: AttendanceExportFile =
            exportMonthlyAttendance(sectionId, september, "content://attendance.xlsx")

        assertThat(file).isEqualTo(AttendanceExportFile("exported.xlsx", "/tmp/exported.xlsx"))
        assertThat(exporter.receivedUri).isEqualTo("content://attendance.xlsx")
        assertThat(exporter.receivedMonth).isEqualTo(september)
        val luzEntry: AttendanceExportEntry = exporter.receivedEntries.single { it.studentCode == luz.code }
        assertThat(luzEntry.statusesByDate).containsEntry(september.atDay(1), AttendanceStatus.PRESENT)
        val joseEntry: AttendanceExportEntry = exporter.receivedEntries.single { it.studentCode == jose.code }
        assertThat(joseEntry.statusesByDate).containsEntry(september.atDay(1), AttendanceStatus.ABSENT)
    }

    @Test
    fun `a student withdrawn before the month never reaches an export entry`() = runTest {
        studentRepository.save(jose.copy(withdrawalDate = september.atDay(1).minusDays(1)))
        attendanceRepository.record(
            record(jose.id, september.minusMonths(1).atEndOfMonth(), AttendanceStatus.PRESENT),
        )

        exportMonthlyAttendance(sectionId, september, "content://attendance.xlsx")

        assertThat(exporter.receivedEntries.map { it.studentCode }).containsExactly(luz.code)
    }

    @Test
    fun `summary and export agree on which students are included for the same section and month`() = runTest {
        val ana: Student = student("student-3", "12345678901236", "CASTRO LEON, Ana")
        studentRepository.save(jose.copy(withdrawalDate = september.atDay(1).minusDays(1)))
        studentRepository.save(ana.copy(withdrawalDate = september.atDay(15)))
        val getMonthlySummary = GetMonthlyAttendanceSummaryUseCase(studentRepository, attendanceRepository)

        val summary: MonthlyAttendanceSummary = getMonthlySummary(sectionId, september).first()
        exportMonthlyAttendance(sectionId, september, "content://attendance.xlsx")

        val summaryIds: Set<StudentId> = summary.rows.map { it.studentId }.toSet()
        val exportIds: Set<StudentId?> = exporter.receivedEntries.map { entry ->
            studentRepository.findByCode(sectionId, entry.studentCode)?.id
        }.toSet()
        assertThat(exportIds).isEqualTo(summaryIds)
        assertThat(exportIds).containsExactly(luz.id, ana.id)
    }
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
