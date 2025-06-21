package com.emm.gema.domain.course

import com.emm.gema.domain.course.model.Course
import com.emm.gema.domain.course.model.CreateCourseInput
import kotlinx.coroutines.flow.Flow

interface CourseRepository {

    suspend fun create(course: CreateCourseInput)

    fun all(): Flow<List<Course>>

    suspend fun find(id: String): Course?
}