package com.emm.gema.core.domain.activity

import com.emm.gema.core.domain.schoolyear.PeriodId
import com.emm.gema.core.domain.section.SectionId
import kotlinx.coroutines.flow.Flow

class GetActivityEvidenceStudentCountsUseCase(
    private val repository: EvidenceLevelRepository,
) {

    operator fun invoke(sectionId: SectionId, periodId: PeriodId): Flow<Map<ActivityId, Int>> =
        repository.observeRecordedStudentCountsByPeriod(sectionId = sectionId, periodId = periodId)
}
