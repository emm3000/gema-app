package com.emm.gema.feature.attendance.day

import java.time.YearMonth

sealed interface AttendanceDayUiEffect {

    data class ShowMessage(val text: String) : AttendanceDayUiEffect

    data class NavigateToAttendanceMonth(val sectionId: String, val month: YearMonth) : AttendanceDayUiEffect

    data object NavigateBack : AttendanceDayUiEffect
}
