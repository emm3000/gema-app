package com.emm.gema.core.domain.activity

import kotlinx.coroutines.flow.Flow

class GetActivitiesUseCase(
    private val repository: ActivityRepository,
) {

    operator fun invoke(sectionId: String, periodId: String): Flow<List<Activity>> =
        repository.observeByPeriod(sectionId, periodId)
}
