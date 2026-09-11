package com.emm.gema.core.domain.evaluation

import kotlinx.coroutines.flow.Flow

class GetRecordedLevelCountsUseCase(
    private val repository: PeriodLevelRepository,
) {

    operator fun invoke(sectionId: String, periodId: String): Flow<Map<String, Int>> =
        repository.observeRecordedCountsByPeriod(sectionId = sectionId, periodId = periodId)
}
