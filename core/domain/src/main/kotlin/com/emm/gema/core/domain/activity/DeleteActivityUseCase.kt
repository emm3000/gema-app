package com.emm.gema.core.domain.activity

class DeleteActivityUseCase(
    private val activityRepository: ActivityRepository,
    private val evidenceLevelRepository: EvidenceLevelRepository,
) {

    suspend operator fun invoke(activityId: ActivityId) {
        evidenceLevelRepository.deleteByActivity(activityId)
        activityRepository.delete(activityId)
    }
}
