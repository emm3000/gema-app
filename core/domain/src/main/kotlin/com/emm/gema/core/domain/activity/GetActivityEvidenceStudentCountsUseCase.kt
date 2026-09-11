package com.emm.gema.core.domain.activity

import kotlinx.coroutines.flow.Flow

class GetActivityEvidenceStudentCountsUseCase(
    private val repository: EvidenceLevelRepository,
) {

    operator fun invoke(sectionId: String, periodId: String): Flow<Map<String, Int>> =
        repository.observeRecordedStudentCountsByPeriod(sectionId = sectionId, periodId = periodId)
}
