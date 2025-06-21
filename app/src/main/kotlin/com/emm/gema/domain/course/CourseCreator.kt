package com.emm.gema.domain.course

import com.emm.gema.domain.course.model.CreateCourseInput

class CourseCreator(private val courseRepository: CourseRepository) {

    suspend fun create(course: CreateCourseInput) {
        courseRepository.create(course)
    }
}