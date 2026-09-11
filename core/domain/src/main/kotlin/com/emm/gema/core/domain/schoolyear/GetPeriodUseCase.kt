package com.emm.gema.core.domain.schoolyear

class GetPeriodUseCase(
    private val repository: PeriodRepository,
) {

    suspend operator fun invoke(periodId: String): Period? = repository.findById(periodId)
}
