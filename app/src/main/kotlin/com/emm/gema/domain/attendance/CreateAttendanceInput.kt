package com.emm.gema.domain.attendance

import java.time.LocalDate

data class CreateAttendanceInput(
    val date: LocalDate,
    val courseId: String,
    val students: List<StudentAttendance>,
)

data class StudentAttendance(
    val studentId: String,
    val status: AttendanceStatus,
)