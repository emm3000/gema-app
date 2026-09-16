package com.emm.gema.core.domain.attendance

import com.emm.gema.core.domain.fake.InMemoryAttendanceRepository
import com.emm.gema.core.domain.fake.InMemoryStudentRepository
import com.emm.gema.core.domain.student.Student
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

class GetMonthlyAttendanceSummaryUseCaseTest {

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

        val summary: MonthlyAttendanceSummary = getMonthlySummary(sectionId, september).first()

        assertThat(summary.recordedDayCount).isEqualTo(2)
        val luzCounts: StudentAttendanceMonthCount = summary.rows.single { it.studentId == luz.id }
        assertThat(luzCounts.countsByStatus[AttendanceStatus.PRESENT]).isEqualTo(1)
        assertThat(luzCounts.countsByStatus[AttendanceStatus.LATE]).isEqualTo(1)
        val joseCounts: StudentAttendanceMonthCount = summary.rows.single { it.studentId == jose.id }
        assertThat(joseCounts.countsByStatus[AttendanceStatus.ABSENT]).isEqualTo(1)
        assertThat(joseCounts.countsByStatus[AttendanceStatus.JUSTIFIED]).isEqualTo(1)
    }

    @Test
    fun `an unmarked day contributes no count to any status`() = runTest {
        val summary: MonthlyAttendanceSummary = getMonthlySummary(sectionId, september).first()

        assertThat(summary.recordedDayCount).isEqualTo(0)
        assertThat(summary.rows.map { it.countsByStatus.values.sum() }).containsExactly(0, 0)
        assertThat(summary.rows.map { it.countsByStatus.keys })
            .containsExactly(AttendanceStatus.entries.toSet(), AttendanceStatus.entries.toSet())
    }

    @Test
    fun `rows are ordered by surname like every other roster`() = runTest {
        val summary: MonthlyAttendanceSummary = getMonthlySummary(sectionId, september).first()

        assertThat(summary.rows.map { it.displayName })
            .containsExactly("ACOSTA RIVERA, Luz Maria", "BAUTISTA QUISPE, Jose").inOrder()
    }

    @Test
    fun `a student withdrawn on the first day of the month is left out of the summary`() = runTest {
        studentRepository.save(jose.copy(withdrawalDate = september.atDay(1)))

        val summary: MonthlyAttendanceSummary = getMonthlySummary(sectionId, september).first()

        assertThat(summary.rows.map { it.studentId }).containsExactly(luz.id)
    }

    @Test
    fun `a student withdrawn on the second day of the month is still present in the summary`() = runTest {
        studentRepository.save(jose.copy(withdrawalDate = september.atDay(2)))

        val summary: MonthlyAttendanceSummary = getMonthlySummary(sectionId, september).first()

        assertThat(summary.rows.map { it.studentId }).containsExactly(luz.id, jose.id)
    }

    @Test
    fun `a student withdrawn during the month is still present in the summary`() = runTest {
        studentRepository.save(jose.copy(withdrawalDate = september.atDay(15)))

        val summary: MonthlyAttendanceSummary = getMonthlySummary(sectionId, september).first()

        assertThat(summary.rows.map { it.studentId }).containsExactly(luz.id, jose.id)
    }

    @Test
    fun `a different month keeps its own records apart`() = runTest {
        attendanceRepository.record(record(luz.id, september.atDay(1), AttendanceStatus.ABSENT))
        attendanceRepository.record(record(luz.id, september.minusMonths(1).atDay(1), AttendanceStatus.LATE))

        val summary: MonthlyAttendanceSummary = getMonthlySummary(sectionId, september).first()

        val luzCounts: Map<AttendanceStatus, Int> = summary.rows.single { it.studentId == luz.id }.countsByStatus
        assertThat(luzCounts[AttendanceStatus.ABSENT]).isEqualTo(1)
        assertThat(luzCounts[AttendanceStatus.LATE]).isEqualTo(0)
    }
}
