package com.emm.gema.domain.attendance

interface AttendanceRepository {

    suspend fun upsert(input: CreateAttendanceInput)
}