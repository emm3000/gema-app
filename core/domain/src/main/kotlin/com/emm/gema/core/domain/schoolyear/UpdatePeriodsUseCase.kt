package com.emm.gema.core.domain.schoolyear

class UpdatePeriodsUseCase(
    private val periodRepository: PeriodRepository,
    private val schoolYearRepository: SchoolYearRepository,
) {

    suspend operator fun invoke(schoolYearId: SchoolYearId, periodDates: List<PeriodDates>) {
        val schoolYear: SchoolYear = requireNotNull(schoolYearRepository.findById(schoolYearId)) {
            "There is no school year ${schoolYearId.value}"
        }
        val stored: List<Period> = periodRepository.findBySchoolYear(schoolYearId)
        val edited: List<Period> = periodDates.map { dates ->
            val period: Period = requireNotNull(stored.find { it.number == dates.number }) {
                "There is no period ${dates.number} in school year ${schoolYearId.value}"
            }
            period.copy(startDate = dates.startDate, endDate = dates.endDate)
        }

        requirePeriodsFit(schoolYear, edited)
        periodRepository.saveAll(edited)
    }
}
