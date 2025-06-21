package com.emm.gema.domain.attendance

import java.time.LocalDate

data class Attendance(
    val id: String,
    val date: LocalDate,
    val status: AttendanceStatus,
    val studentId: String,
    val courseId: String,
)
