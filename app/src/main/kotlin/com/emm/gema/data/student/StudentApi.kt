package com.emm.gema.data.student

import com.emm.gema.data.attendance.StudentResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface StudentApi {

    @POST("student/create-student-with-course")
    suspend fun createStudentWithCourse(@Body request: CreateStudentRequest)

    @GET("student/students-with-course/{courseId}")
    suspend fun studentsByCourseId(
        @Path("courseId") courseId: String
    ): List<StudentResponse>
}