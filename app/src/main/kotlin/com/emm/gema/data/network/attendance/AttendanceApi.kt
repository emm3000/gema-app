package com.emm.gema.data.network.attendance

import kotlinx.serialization.json.JsonArray
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query

interface AttendanceApi {

    @GET("attendance/course/{courseId}")
    suspend fun attendance(
        @Path("courseId") courseId: String,
        @Query("date") date: String,
    ): List<AttendanceResponse>

    @PATCH("attendance")
    suspend fun create(@Body request: AttendanceRequest): JsonArray
}