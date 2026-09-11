package com.emm.gema.core.domain.attendance

import java.time.LocalDate

data class AttendanceRecord(
    val sectionId: String,
    val studentId: String,
    val date: LocalDate,
    val status: AttendanceStatus,
) {
    init {
        require(sectionId.isNotBlank()) { "An attendance record belongs to a section" }
        require(studentId.isNotBlank()) { "An attendance record belongs to a student" }
    }
}
