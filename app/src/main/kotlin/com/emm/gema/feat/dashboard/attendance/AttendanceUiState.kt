package com.emm.gema.feat.dashboard.attendance

import com.emm.gema.domain.attendance.StudentAttendanceStatus
import com.emm.gema.domain.course.model.Course
import java.time.LocalDate

data class AttendanceUiState(
    val courses: List<Course> = emptyList(),
    val courseSelected: Course? = null,
    val datePicker: LocalDate = LocalDate.now(),
    val isSubmitButtonEnabled: Boolean = false,
    val attendance: List<StudentAttendanceStatus> = emptyList(),
    val screenState: ScreenState = ScreenState.None,
)