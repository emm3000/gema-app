package com.emm.gema.core.domain.attendance

import java.time.Clock
import java.time.LocalDate

class RecordAttendanceUseCase(
    private val repository: AttendanceRepository,
    private val clock: Clock,
) {

    suspend operator fun invoke(sectionId: String, studentId: String, date: LocalDate, status: AttendanceStatus) {
        require(!date.isAfter(LocalDate.now(clock))) { "Attendance cannot be taken for a future date" }
        repository.record(
            AttendanceRecord(sectionId = sectionId, studentId = studentId, date = date, status = status),
        )
    }
}
