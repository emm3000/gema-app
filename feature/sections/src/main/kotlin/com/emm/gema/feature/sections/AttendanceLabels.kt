package com.emm.gema.feature.sections

import com.emm.gema.core.domain.attendance.AttendanceDaySummary

fun AttendanceDaySummary.attendanceSummaryLabel(): String =
    if (isTaken) "$presentCount de $totalCount presentes" else "Sin tomar"
