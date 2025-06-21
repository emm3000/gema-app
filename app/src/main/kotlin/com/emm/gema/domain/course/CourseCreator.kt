package com.emm.gema.domain.course

class CourseCreator(private val courseRepository: CourseRepository) {

    suspend fun create(course: Course): Course {
        return courseRepository.create(course)
    }
}