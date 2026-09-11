package com.emm.gema.core.domain.schoolyear

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetActiveSchoolYearUseCase(
    private val activeSchoolYearRepository: ActiveSchoolYearRepository,
    private val schoolYearRepository: SchoolYearRepository,
) {

    operator fun invoke(): Flow<SchoolYear?> = combine(
        activeSchoolYearRepository.observeActiveId(),
        schoolYearRepository.observeAll(),
    ) { activeId, schoolYears ->
        schoolYears.find { it.id == activeId } ?: schoolYears.firstOrNull()
    }
}
