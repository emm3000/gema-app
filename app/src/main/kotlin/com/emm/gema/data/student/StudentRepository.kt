package com.emm.gema.data.student

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StudentRepository(private val api: StudentApi) {

    suspend fun createStudentWithCourse(
        request: CreateStudentRequest,
    ) = withContext(Dispatchers.IO) {
        return@withContext api.createStudentWithCourse(request)
    }
}