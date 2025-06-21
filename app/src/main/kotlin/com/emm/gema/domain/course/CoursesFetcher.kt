package com.emm.gema.domain.course

import kotlinx.coroutines.flow.Flow

class CoursesFetcher(private val courseRepository: CourseRepository) {

    suspend fun fetchAll(): Flow<List<Course>> {
        return courseRepository.all()
    }
}