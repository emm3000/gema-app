package com.emm.gema.core.domain.schoolyear

class GetPeriodUseCase(
    private val repository: PeriodRepository,
) {

    suspend operator fun invoke(periodId: PeriodId): Period? = repository.findById(periodId)
}
