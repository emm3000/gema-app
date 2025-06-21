package com.emm.gema.feat.dashboard.attendance

import com.emm.gema.domain.attendance.StudentAttendanceStatus
import com.emm.gema.domain.course.model.Course
import java.time.LocalDate

sealed interface AttendanceAction {

    class OnDateChange(val date: LocalDate) : AttendanceAction

    class OnCourseSelectedChange(val course: Course): AttendanceAction

    class OnAttendanceStatusChange(val student: StudentAttendanceStatus) : AttendanceAction

    object OnSave: AttendanceAction
}