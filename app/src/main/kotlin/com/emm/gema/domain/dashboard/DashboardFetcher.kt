package com.emm.gema.domain.dashboard

import com.emm.gema.domain.attendance.AttendanceRepository
import com.emm.gema.domain.course.CoursesFetcher
import com.emm.gema.domain.course.model.Course
import com.emm.gema.domain.evaluation.Evaluation
import com.emm.gema.domain.evaluation.EvaluationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate

class DashboardFetcher(
    private val coursesFetcher: CoursesFetcher,
    private val evaluationRepository: EvaluationRepository,
    private val attendanceRepository: AttendanceRepository,
) {

    fun fetch(): Flow<Dashboard> {
        val courses: Flow<List<Course>> = coursesFetcher.fetchAll()
        val evaluations: Flow<List<Evaluation>> = evaluationRepository.fetchAll()
        val attendance: Flow<List<AttendanceToday>> = attendanceRepository.attendanceToday(LocalDate.now())

        return combine(
            courses,
            evaluations,
            attendance
        ) { courses, evaluations, attendance ->
            Dashboard(
                courses = courses,
                evaluations = evaluations,
                attendance = attendance
            )
        }
    }
}