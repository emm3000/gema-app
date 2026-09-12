package com.emm.gema.feature.attendance

import com.emm.gema.core.domain.attendance.AttendanceStatus
import com.emm.gema.core.ui.GAttendanceOption

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
