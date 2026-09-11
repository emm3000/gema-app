package com.emm.gema.core.domain.setup

import com.emm.gema.core.domain.id.IdGenerator
import com.emm.gema.core.domain.schoolyear.Period
import com.emm.gema.core.domain.schoolyear.PeriodDates
import com.emm.gema.core.domain.schoolyear.PeriodKind
import com.emm.gema.core.domain.schoolyear.SchoolYear
import com.emm.gema.core.domain.schoolyear.divide
import com.emm.gema.core.domain.schoolyear.requirePeriodsFit
import com.emm.gema.core.domain.section.Grade
import com.emm.gema.core.domain.section.Section
import java.time.LocalDate

class CompleteSetupUseCase(
    private val repository: SetupRepository,
    private val idGenerator: IdGenerator,
) {

    suspend operator fun invoke(
        yearLabel: String,
        startDate: LocalDate,
        endDate: LocalDate,
        periodKind: PeriodKind,
        grade: Grade,
        sectionName: String,
        periodDates: List<PeriodDates> = periodKind.divide(startDate, endDate),
    ): SchoolYear {
        val schoolYear = SchoolYear(
            id = idGenerator.newId(),
            label = yearLabel.trim(),
            startDate = startDate,
            endDate = endDate,
            periodKind = periodKind,
        )
        val periods: List<Period> = periodDates.map { dates ->
            Period(
                id = idGenerator.newId(),
                schoolYearId = schoolYear.id,
                number = dates.number,
                startDate = dates.startDate,
                endDate = dates.endDate,
            )
        }
        val section = Section(
            id = idGenerator.newId(),
            schoolYearId = schoolYear.id,
            grade = grade,
            name = sectionName.trim(),
        )
        requirePeriodsFit(schoolYear, periods)
        repository.saveAndActivate(schoolYear, periods, section)
        return schoolYear
    }
}
