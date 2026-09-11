package com.emm.gema.feature.attendance.day

import com.emm.gema.core.domain.section.SectionId
import java.time.YearMonth

sealed interface AttendanceDayUiEffect {

    data class ShowMessage(val message: AttendanceDayMessage) : AttendanceDayUiEffect

    data class NavigateToAttendanceMonth(val sectionId: SectionId, val month: YearMonth) : AttendanceDayUiEffect

    data object NavigateBack : AttendanceDayUiEffect
}
