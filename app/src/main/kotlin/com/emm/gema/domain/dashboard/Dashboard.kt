package com.emm.gema.domain.dashboard

import com.emm.gema.domain.course.model.Course
import com.emm.gema.domain.evaluation.Evaluation

data class Dashboard(
    val courses: List<Course>,
    val evaluations: List<Evaluation>,
    val attendance: List<AttendanceToday>,
) {

    companion object {

        val Empty = Dashboard(
            courses = emptyList(),
            evaluations = emptyList(),
            attendance = emptyList(),
        )
    }
}