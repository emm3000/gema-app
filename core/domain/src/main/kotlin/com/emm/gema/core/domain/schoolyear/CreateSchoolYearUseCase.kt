package com.emm.gema.core.domain.schoolyear

import com.emm.gema.core.domain.id.IdGenerator
import java.time.LocalDate

class CreateSchoolYearUseCase(
    private val repository: SchoolYearRepository,
    private val idGenerator: IdGenerator,
) {

    suspend operator fun invoke(
        startDate: LocalDate,
        endDate: LocalDate,
        periodKind: PeriodKind,
    ): SchoolYear {
        val schoolYear = SchoolYear(
            id = idGenerator.newId(),
            startDate = startDate,
            endDate = endDate,
            periodKind = periodKind,
        )
        repository.save(schoolYear)
        return schoolYear
    }
}
