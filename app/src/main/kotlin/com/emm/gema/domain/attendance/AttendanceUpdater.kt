package com.emm.gema.domain.attendance

class AttendanceUpdater(private val attendanceRepository: AttendanceRepository) {

    suspend fun update(input: CreateAttendanceInput) {
        attendanceRepository.upsert(input)
    }
}