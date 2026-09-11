package com.emm.gema.core.domain.evaluation

class GetPeriodLevelUseCase(
    private val repository: PeriodLevelRepository,
) {

    suspend operator fun invoke(key: PeriodLevelKey): PeriodLevel = repository.find(key) ?: PeriodLevel(key)
}
