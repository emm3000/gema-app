package com.emm.gema.core.domain.attendance

data class AttendanceDaySummary(
    val presentCount: Int,
    val totalCount: Int,
    val unmarkedCount: Int,
) {
    val isTaken: Boolean get() = unmarkedCount < totalCount
}

fun List<AttendanceEntry>.summarise(): AttendanceDaySummary = AttendanceDaySummary(
    presentCount = count { it.status == AttendanceStatus.PRESENT },
    totalCount = size,
    unmarkedCount = count { !it.isRecorded },
)
