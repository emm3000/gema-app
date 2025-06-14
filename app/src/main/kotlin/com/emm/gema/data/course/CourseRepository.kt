package com.emm.gema.data.course

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CourseRepository(private val courseApi: CourseApi) {

    suspend fun create(request: CreateCourseRequest) = withContext(Dispatchers.IO) {
        courseApi.create(request)
    }
}