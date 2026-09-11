package com.emm.gema.core.domain.schoolyear

import java.time.LocalDate

class FindPeriodForDateUseCase(
    private val repository: PeriodRepository,
) {

    suspend operator fun invoke(schoolYearId: String, date: LocalDate): Period? =
        repository.findBySchoolYear(schoolYearId).find { it.contains(date) }
}
