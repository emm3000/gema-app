package com.emm.gema.feature.attendance

import com.emm.gema.core.domain.attendance.AttendanceStatus
import com.emm.gema.core.domain.section.Section
import com.emm.gema.core.ui.GAttendanceOption
import java.time.LocalDate

private val dayNames: List<String> = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")

private val monthNames: List<String> = listOf(
    "ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "set", "oct", "nov", "dic",
)

fun Section.title(): String = "${grade.number}° $name"

fun LocalDate.asDayLabel(): String =
    "${dayNames[dayOfWeek.value - 1]} $dayOfMonth ${monthNames[monthValue - 1]} $year"

fun AttendanceStatus.asToggleOption(): GAttendanceOption = when (this) {
    AttendanceStatus.PRESENT -> GAttendanceOption.PRESENT
    AttendanceStatus.LATE -> GAttendanceOption.LATE
    AttendanceStatus.ABSENT -> GAttendanceOption.ABSENT
    AttendanceStatus.JUSTIFIED -> GAttendanceOption.JUSTIFIED
}

fun GAttendanceOption.asStatus(): AttendanceStatus = when (this) {
    GAttendanceOption.PRESENT -> AttendanceStatus.PRESENT
    GAttendanceOption.LATE -> AttendanceStatus.LATE
    GAttendanceOption.ABSENT -> AttendanceStatus.ABSENT
    GAttendanceOption.JUSTIFIED -> AttendanceStatus.JUSTIFIED
}
