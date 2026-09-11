package com.emm.gema.feature.attendance.month

sealed interface AttendanceMonthUiEffect {

    data class OpenDocumentPicker(val mimeTypes: List<String>) : AttendanceMonthUiEffect

    data class ShareFile(val path: String, val mimeType: String) : AttendanceMonthUiEffect

    data class ShowMessage(val text: String) : AttendanceMonthUiEffect

    data object NavigateBack : AttendanceMonthUiEffect
}
