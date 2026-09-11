package com.emm.gema.core.domain.schoolyear

import kotlinx.coroutines.flow.Flow

class GetPeriodsUseCase(
    private val repository: PeriodRepository,
) {

    operator fun invoke(schoolYearId: SchoolYearId): Flow<List<Period>> =
        repository.observeBySchoolYear(schoolYearId)
}
