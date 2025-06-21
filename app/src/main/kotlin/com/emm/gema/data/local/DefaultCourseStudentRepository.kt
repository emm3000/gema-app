package com.emm.gema.data.local

import com.emm.gema.StudentCourseQueries
import com.emm.gema.domain.course.CourseStudentRepository

class DefaultCourseStudentRepository(
    private val dao: StudentCourseQueries,
): CourseStudentRepository {

    override suspend fun addStudentsToCourse(courseId: String, studentId: String) {
        dao.insert(
            courseId = courseId,
            studentId = studentId,
        )
    }
}