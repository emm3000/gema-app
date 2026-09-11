package com.emm.gema.feature.sections

import com.emm.gema.core.domain.attendance.AttendanceDaySummary
import com.emm.gema.core.theme.DayNames
import com.emm.gema.core.theme.MonthNames
import java.time.LocalDate

fun LocalDate.asDayLabel(): String =
    "${DayNames.full[dayOfWeek.value - 1]} $dayOfMonth de ${MonthNames.full[monthValue - 1]}"

fun AttendanceDaySummary.attendanceSummaryLabel(): String =
    if (isTaken) "$presentCount de $totalCount presentes" else "Sin tomar"
