package com.emm.gema.core.domain.schoolyear

import java.time.LocalDate

class UpdatePeriodDatesUseCase(
    private val periodRepository: PeriodRepository,
    private val schoolYearRepository: SchoolYearRepository,
) {

    suspend operator fun invoke(periodId: String, startDate: LocalDate, endDate: LocalDate) {
        val period: Period = requireNotNull(periodRepository.findById(periodId)) {
            "There is no period $periodId"
        }
        val schoolYear: SchoolYear = requireNotNull(schoolYearRepository.findById(period.schoolYearId)) {
            "There is no school year ${period.schoolYearId}"
        }
        val edited: Period = period.copy(startDate = startDate, endDate = endDate)
        val siblings: List<Period> = periodRepository.findBySchoolYear(schoolYear.id)
            .filterNot { it.id == edited.id }

        requirePeriodsFit(schoolYear, siblings + edited)
        periodRepository.saveAll(listOf(edited))
    }
}
