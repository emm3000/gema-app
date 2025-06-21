package com.emm.gema.domain.attendance

import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface AttendanceRepository {

    suspend fun upsert(input: CreateAttendanceInput)

    fun selectByDateAndCourse(courseId: String, date: LocalDate): Flow<List<Attendance>>
}