package com.emm.gema.domain.attendance

import com.emm.gema.domain.dashboard.AttendanceToday
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface AttendanceRepository {

    suspend fun upsert(input: CreateAttendanceInput)

    fun selectByDateAndCourse(courseId: String, date: LocalDate): Flow<List<Attendance>>

    fun attendanceToday(date: LocalDate): Flow<List<AttendanceToday>>
}