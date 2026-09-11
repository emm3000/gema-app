package com.emm.gema.core.domain.schoolyear

import java.time.Clock
import java.time.LocalDate

class GetCurrentPeriodUseCase(
    private val repository: PeriodRepository,
    private val clock: Clock,
) {

    suspend operator fun invoke(schoolYearId: SchoolYearId): Period? {
        val today: LocalDate = LocalDate.now(clock)
        return repository.findBySchoolYear(schoolYearId).find { it.contains(today) }
    }
}
