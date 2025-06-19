package com.emm.gema.data.student

import retrofit2.http.Body
import retrofit2.http.POST

interface StudentApi {

    @POST("student/create-student-with-course")
    suspend fun createStudentWithCourse(@Body request: CreateStudentRequest): Unit
}