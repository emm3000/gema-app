package com.emm.gema.feature.sections

import com.emm.gema.core.domain.attendance.AttendanceDaySummary
import com.emm.gema.core.theme.DayNames
import java.time.LocalDate

private val monthNames: List<String> = listOf(
    "enero", "febrero", "marzo", "abril", "mayo", "junio",
    "julio", "agosto", "setiembre", "octubre", "noviembre", "diciembre",
)

fun LocalDate.asDayLabel(): String =
    "${DayNames.full[dayOfWeek.value - 1]} $dayOfMonth de ${monthNames[monthValue - 1]}"

fun AttendanceDaySummary.attendanceSummaryLabel(): String =
    if (isTaken) "$presentCount de $totalCount presentes" else "Sin tomar"
