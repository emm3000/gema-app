package com.emm.gema.feature.attendance.day

sealed interface AttendanceDayUiEffect {

    data class ShowMessage(val text: String) : AttendanceDayUiEffect

    data object NavigateBack : AttendanceDayUiEffect
}
