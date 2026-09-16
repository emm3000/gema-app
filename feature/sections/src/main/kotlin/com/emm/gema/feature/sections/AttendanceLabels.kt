package com.emm.gema.feature.sections

import com.emm.gema.core.domain.attendance.AttendanceDaySummary

const val ATTENDANCE_UNTAKEN_LABEL: String = "Sin tomar"

fun AttendanceDaySummary.attendanceSummaryLabel(): String =
    if (isTaken) "$presentCount de $totalCount presentes" else ATTENDANCE_UNTAKEN_LABEL
