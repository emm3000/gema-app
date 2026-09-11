package com.emm.gema.core.domain.attendance

import com.emm.gema.core.domain.student.StudentId

data class StudentAttendanceMonthCount(
    val studentId: StudentId,
    val displayName: String,
    val countsByStatus: Map<AttendanceStatus, Int>,
)

data class MonthlyAttendanceSummary(
    val recordedDayCount: Int,
    val rows: List<StudentAttendanceMonthCount>,
)
