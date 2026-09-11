package com.emm.gema.core.domain.schoolyear

import kotlinx.coroutines.flow.Flow

class GetSchoolYearsUseCase(
    private val repository: SchoolYearRepository,
) {

    operator fun invoke(): Flow<List<SchoolYear>> = repository.observeAll()
}
