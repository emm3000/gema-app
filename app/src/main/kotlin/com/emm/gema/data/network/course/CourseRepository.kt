package com.emm.gema.data.network.course

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CourseRepository(private val courseApi: CourseApi) {

    suspend fun create(request: CreateCourseRequest) = withContext(Dispatchers.IO) {
        courseApi.create(request)
    }

    suspend fun all(): List<CourseResponse> {
        return withContext(Dispatchers.IO) {
            courseApi.all()
        }
    }
}