package com.emm.gema.feature.attendance.day

import java.time.YearMonth

sealed interface AttendanceDayUiEffect {

    data class ShowMessage(val message: AttendanceDayMessage) : AttendanceDayUiEffect

    data class NavigateToAttendanceMonth(val sectionId: String, val month: YearMonth) : AttendanceDayUiEffect

    data object NavigateBack : AttendanceDayUiEffect
}
