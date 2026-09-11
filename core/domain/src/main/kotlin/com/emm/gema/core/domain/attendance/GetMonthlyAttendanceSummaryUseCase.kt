package com.emm.gema.core.domain.attendance

import com.emm.gema.core.domain.section.SectionId
import com.emm.gema.core.domain.student.Student
import com.emm.gema.core.domain.student.StudentRepository
import com.emm.gema.core.domain.student.orderedByName
import java.time.YearMonth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetMonthlyAttendanceSummaryUseCase(
    private val studentRepository: StudentRepository,
    private val attendanceRepository: AttendanceRepository,
) {

    operator fun invoke(sectionId: SectionId, month: YearMonth): Flow<MonthlyAttendanceSummary> = combine(
        studentRepository.observeBySection(sectionId),
        attendanceRepository.observeBySectionAndMonth(sectionId, month),
    ) { students: List<Student>, records: List<AttendanceRecord> ->
        MonthlyAttendanceSummary(
            recordedDayCount = records.map { it.date }.distinct().size,
            rows = students.filter { it.attends(month) }.orderedByName().map { it.countsOf(records) },
        )
    }

    private fun Student.attends(month: YearMonth): Boolean =
        withdrawalDate == null || withdrawalDate.isAfter(month.atDay(1))

    private fun Student.countsOf(records: List<AttendanceRecord>): StudentAttendanceMonthCount {
        val own: List<AttendanceRecord> = records.filter { it.studentId == id }
        return StudentAttendanceMonthCount(
            studentId = id,
            displayName = fullName,
            countsByStatus = AttendanceStatus.entries.associateWith { status ->
                own.count { it.status == status }
            },
        )
    }
}
