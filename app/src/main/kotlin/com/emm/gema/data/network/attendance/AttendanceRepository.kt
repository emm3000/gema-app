package com.emm.gema.data.network.attendance

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AttendanceRepository(private val attendanceApi: AttendanceApi) {

    suspend fun all(courseId: String, date: String): List<AttendanceResponse> = withContext(Dispatchers.IO) {
        attendanceApi.attendance(courseId, date)
    }

    suspend fun create(request: AttendanceRequest): Any = withContext(Dispatchers.IO) {
        attendanceApi.create(request)
    }
}