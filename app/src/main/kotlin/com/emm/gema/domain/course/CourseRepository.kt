package com.emm.gema.domain.course

import kotlinx.coroutines.flow.Flow

interface CourseRepository {

    suspend fun create(course: Course): Course

    fun all(): Flow<List<Course>>

    suspend fun find(id: String): Course?
}