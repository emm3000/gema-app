package com.emm.gema.core.domain.attendance

object AttendanceSiagieCode {

    private val codes: Map<AttendanceStatus, String> = mapOf(
        AttendanceStatus.PRESENT to "P",
        AttendanceStatus.LATE to "T",
        AttendanceStatus.ABSENT to "F",
        AttendanceStatus.JUSTIFIED to "FJ",
    )

    fun of(status: AttendanceStatus): String = codes.getValue(status)
}
