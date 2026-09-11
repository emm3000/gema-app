package com.emm.gema.core.domain.section

import com.emm.gema.core.domain.attendance.CountAttendanceDaysUseCase
import com.emm.gema.core.domain.evaluation.GetPeriodLevelCountUseCase
import com.emm.gema.core.domain.student.GetStudentsUseCase
import kotlinx.coroutines.flow.first

data class SectionDeletionImpact(
    val studentCount: Int,
    val attendanceDayCount: Int,
    val periodLevelCount: Int,
)

class GetSectionDeletionImpactUseCase(
    private val getStudents: GetStudentsUseCase,
    private val getPeriodLevelCount: GetPeriodLevelCountUseCase,
    private val countAttendanceDays: CountAttendanceDaysUseCase,
) {

    suspend operator fun invoke(sectionId: SectionId): SectionDeletionImpact = SectionDeletionImpact(
        studentCount = getStudents(sectionId).first().size,
        attendanceDayCount = countAttendanceDays(sectionId),
        periodLevelCount = getPeriodLevelCount(sectionId).first(),
    )
}
