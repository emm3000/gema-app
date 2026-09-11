package com.emm.gema.core.domain.attendance

data class StudentAttendanceMonthCount(
    val studentId: String,
    val displayName: String,
    val countsByStatus: Map<AttendanceStatus, Int>,
)

data class MonthlyAttendanceSummary(
    val recordedDayCount: Int,
    val rows: List<StudentAttendanceMonthCount>,
)
