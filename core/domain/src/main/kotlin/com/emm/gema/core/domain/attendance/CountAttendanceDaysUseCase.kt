package com.emm.gema.core.domain.attendance

import com.emm.gema.core.domain.section.SectionId

class CountAttendanceDaysUseCase(
    private val repository: AttendanceRepository,
) {

    suspend operator fun invoke(sectionId: SectionId): Int = repository.countRecordedDays(sectionId)
}
