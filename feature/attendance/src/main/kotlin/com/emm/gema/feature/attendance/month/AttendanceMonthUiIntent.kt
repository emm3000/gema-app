package com.emm.gema.feature.attendance.month

import java.time.YearMonth

sealed interface AttendanceMonthUiIntent {

    data object PreviousMonthClicked : AttendanceMonthUiIntent

    data object NextMonthClicked : AttendanceMonthUiIntent

    data class MonthPicked(val value: YearMonth) : AttendanceMonthUiIntent

    data object ExportClicked : AttendanceMonthUiIntent

    data class TemplatePicked(val uri: String) : AttendanceMonthUiIntent

    data object BackClicked : AttendanceMonthUiIntent
}
