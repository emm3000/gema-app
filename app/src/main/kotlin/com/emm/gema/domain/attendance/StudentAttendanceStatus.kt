package com.emm.gema.domain.attendance

import com.emm.gema.domain.student.Student

data class StudentAttendanceStatus(
    val studentId: String,
    val student: Student,
    val status: AttendanceStatus
)