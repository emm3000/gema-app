package com.emm.gema.feature.attendance

import com.emm.gema.core.domain.attendance.AttendanceStatus
import com.emm.gema.core.domain.section.Section
import com.emm.gema.core.theme.DayNames
import com.emm.gema.core.ui.GAttendanceOption
import java.time.LocalDate
import java.time.YearMonth

private const val ABBREVIATED_DAY_NAME_LENGTH: Int = 3

private val monthNames: List<String> = listOf(
    "ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "set", "oct", "nov", "dic",
)

private val fullMonthNames: List<String> = listOf(
    "enero", "febrero", "marzo", "abril", "mayo", "junio",
    "julio", "agosto", "setiembre", "octubre", "noviembre", "diciembre",
)

fun Section.title(): String = "${grade.number}° $name"

fun LocalDate.asDayLabel(): String {
    val dayName: String = DayNames.full[dayOfWeek.value - 1].take(ABBREVIATED_DAY_NAME_LENGTH)
    return "$dayName $dayOfMonth ${monthNames[monthValue - 1]} $year"
}

fun YearMonth.asMonthLabel(): String = "${fullMonthNames[monthValue - 1]} $year"

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
