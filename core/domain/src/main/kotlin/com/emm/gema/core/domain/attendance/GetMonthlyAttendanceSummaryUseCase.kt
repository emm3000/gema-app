package com.emm.gema.core.domain.attendance

import com.emm.gema.core.domain.student.Student
import com.emm.gema.core.domain.student.StudentRepository
import com.emm.gema.core.domain.student.orderedByName
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.YearMonth

class GetMonthlyAttendanceSummaryUseCase(
    private val studentRepository: StudentRepository,
    private val attendanceRepository: AttendanceRepository,
) {

    operator fun invoke(sectionId: String, month: YearMonth): Flow<MonthlyAttendanceSummary> = combine(
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
            presentCount = own.count { it.status == AttendanceStatus.PRESENT },
            lateCount = own.count { it.status == AttendanceStatus.LATE },
            absentCount = own.count { it.status == AttendanceStatus.ABSENT },
            justifiedCount = own.count { it.status == AttendanceStatus.JUSTIFIED },
        )
    }
}
