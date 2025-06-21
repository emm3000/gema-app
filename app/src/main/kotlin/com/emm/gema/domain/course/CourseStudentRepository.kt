package com.emm.gema.domain.course

interface CourseStudentRepository {

    suspend fun addStudentsToCourse(courseId: String, studentId: String)
}