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

data class CompleteSetupRequest(
    val yearLabel: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val periodKind: PeriodKind,
    val grade: Grade,
    val sectionName: String,
    val periodDates: List<PeriodDates> = periodKind.divide(startDate, endDate),
)

class CompleteSetupUseCase(
    private val repository: SetupRepository,
    private val idGenerator: IdGenerator,
) {

    suspend operator fun invoke(request: CompleteSetupRequest): CompletedSetup {
        val schoolYear = SchoolYear(
            id = idGenerator.newId(),
            label = request.yearLabel.trim(),
            startDate = request.startDate,
            endDate = request.endDate,
            periodKind = request.periodKind,
        )
        val periods: List<Period> = request.periodDates.map { dates ->
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
            grade = request.grade,
            name = request.sectionName.trim(),
        )
        requirePeriodsFit(schoolYear, periods)
        repository.saveAndActivate(schoolYear, periods, section)
        return CompletedSetup(schoolYear, section)
    }
}
