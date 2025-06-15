package com.emm.gema.data.attendance

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AttendanceRepository(private val attendanceApi: AttendanceApi) {

    suspend fun all(courseId: String, date: String): List<AttendanceResponse> = withContext(Dispatchers.IO) {
        attendanceApi.attendance(courseId, date)
    }
}