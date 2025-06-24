package com.emm.gema.feat.dashboard.attendance

import com.emm.gema.domain.attendance.StudentAttendanceStatus
import com.emm.gema.domain.course.model.Course
import java.time.LocalDate

sealed interface AttendanceAction {

    class OnDateChange(val date: LocalDate) : AttendanceAction

    class OnCourseSelected(val course: Course): AttendanceAction

    class OnStatusChange(val student: StudentAttendanceStatus) : AttendanceAction

    object OnSave: AttendanceAction
}