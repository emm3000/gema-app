package com.emm.gema.data.attendance

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface AttendanceApi {

    @GET("attendance/course/{courseId}")
    suspend fun attendance(
        @Path("courseId") courseId: String,
        @Query("date") date: String,
    ): List<AttendanceResponse>
}