package com.emm.gema.domain.course

class CourseStudentAdder(private val courseRepository: CourseStudentRepository) {

    suspend fun addStudents(courseId: String, studentIds: List<String>) {
        courseRepository.addStudentsToCourse(courseId, studentIds)
    }
}