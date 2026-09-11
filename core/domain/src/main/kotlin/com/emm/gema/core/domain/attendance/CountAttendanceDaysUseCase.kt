package com.emm.gema.core.domain.attendance

class CountAttendanceDaysUseCase(
    private val repository: AttendanceRepository,
) {

    suspend operator fun invoke(sectionId: String): Int = repository.countRecordedDays(sectionId)
}
