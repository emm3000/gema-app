package com.emm.gema.core.domain.activity

class GetActivityUseCase(
    private val repository: ActivityRepository,
) {

    suspend operator fun invoke(activityId: ActivityId): Activity? = repository.findById(activityId)
}
