package com.emm.gema.feature.attendance.day

import com.emm.gema.core.domain.attendance.AttendanceStatus
import com.emm.gema.core.domain.student.StudentId
import java.time.LocalDate

sealed interface AttendanceDayUiIntent {

    data class StatusSelected(val studentId: StudentId, val status: AttendanceStatus) : AttendanceDayUiIntent

    data class DatePicked(val value: LocalDate) : AttendanceDayUiIntent

    data object PreviousDayClicked : AttendanceDayUiIntent

    data object NextDayClicked : AttendanceDayUiIntent

    data object MarkAllPresent : AttendanceDayUiIntent

    data object MonthlySummaryClicked : AttendanceDayUiIntent

    data object BackClicked : AttendanceDayUiIntent
}
