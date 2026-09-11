package com.emm.gema.core.domain.evaluation

class SavePeriodLevelUseCase(
    private val repository: PeriodLevelRepository,
) {

    suspend operator fun invoke(periodLevel: PeriodLevel) {
        if (periodLevel.isEmpty) {
            repository.delete(periodLevel.key)
        } else {
            repository.save(periodLevel)
        }
    }
}
