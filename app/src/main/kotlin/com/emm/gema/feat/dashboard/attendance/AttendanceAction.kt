package com.emm.gema.feat.dashboard.attendance

import com.emm.gema.data.course.CourseResponse
import java.time.LocalDate

sealed interface AttendanceAction {

    class OnDateChange(val date: LocalDate) : AttendanceAction

    class OnCourseSelectedChange(val course: CourseResponse): AttendanceAction

    class OnAttendanceStatusChange(val student: Student) : AttendanceAction

    object RetryFetchStudents: AttendanceAction

    object OnSave: AttendanceAction
}