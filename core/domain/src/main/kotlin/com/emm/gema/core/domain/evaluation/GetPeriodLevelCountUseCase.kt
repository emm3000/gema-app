package com.emm.gema.core.domain.evaluation

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetPeriodLevelCountUseCase(
    private val repository: PeriodLevelRepository,
) {

    operator fun invoke(sectionId: String): Flow<Int> = repository
        .observeRecordedCountsBySection(sectionId)
        .map { counts -> counts.values.sum() }
}
