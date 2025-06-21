package com.emm.gema.data.network.student

import com.emm.gema.data.network.attendance.StudentResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StudentRepository(private val api: StudentApi) {

    suspend fun createStudentWithCourse(
        request: CreateStudentRequest,
    ) = withContext(Dispatchers.IO) {
        api.createStudentWithCourse(request)
    }

    suspend fun studentsByCourseId(courseId: String): List<StudentResponse> = withContext(Dispatchers.IO) {
        api.studentsByCourseId(courseId)
    }
}