package com.emm.gema.core.domain.attendance

import com.emm.gema.core.domain.student.Student

data class AttendanceEntry(
    val student: Student,
    val status: AttendanceStatus,
    val isRecorded: Boolean,
)
