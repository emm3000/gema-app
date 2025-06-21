package com.emm.gema.domain.course

import com.emm.gema.domain.course.model.Course
import kotlinx.coroutines.flow.Flow

class CoursesFetcher(private val courseRepository: CourseRepository) {

    suspend fun fetchAll(): Flow<List<Course>> {
        return courseRepository.all()
    }
}