package com.emm.gema.domain.course

class CourseStudentAdder(private val courseRepository: CourseStudentRepository) {

    suspend fun addStudent(courseId: String, studentId: String) {
        courseRepository.addStudentsToCourse(courseId, studentId)
    }
}