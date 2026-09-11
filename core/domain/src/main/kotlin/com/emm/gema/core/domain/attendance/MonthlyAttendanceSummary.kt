package com.emm.gema.core.domain.attendance

data class StudentAttendanceMonthCount(
    val studentId: String,
    val displayName: String,
    val presentCount: Int,
    val lateCount: Int,
    val absentCount: Int,
    val justifiedCount: Int,
)

data class MonthlyAttendanceSummary(
    val recordedDayCount: Int,
    val rows: List<StudentAttendanceMonthCount>,
)
