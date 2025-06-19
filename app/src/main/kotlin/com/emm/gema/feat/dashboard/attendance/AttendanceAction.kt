package com.emm.gema.feat.dashboard.attendance

import com.emm.gema.data.course.CourseResponse
import java.time.LocalDate

interface AttendanceAction {

    class OnDateChange(val date: LocalDate) : AttendanceAction

    class OnCourseSelectedChange(val course: CourseResponse): AttendanceAction

    object RetryFetchStudents: AttendanceAction
}