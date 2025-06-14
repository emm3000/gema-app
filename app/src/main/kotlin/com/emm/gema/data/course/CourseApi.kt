package com.emm.gema.data.course

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface CourseApi {

    @POST("course")
    suspend fun create(@Body request: CreateCourseRequest)

    @GET("course")
    suspend fun all(): List<CourseResponse>
}