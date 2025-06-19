package com.emm.gema.data.student

import com.emm.gema.data.attendance.StudentResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class StudentRepository(private val api: StudentApi) {

    suspend fun createStudentWithCourse(
        request: CreateStudentRequest,
    ) = withContext(Dispatchers.IO) {
        api.createStudentWithCourse(request)
    }

    suspend fun studentsByCourseId(courseId: String): List<StudentResponse> = withContext(Dispatchers.IO) {
        delay(2000L)
        api.studentsByCourseId(courseId)
    }
}