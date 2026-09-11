package com.emm.gema.core.domain.schoolyear

class SwitchSchoolYearUseCase(
    private val repository: ActiveSchoolYearRepository,
) {

    suspend operator fun invoke(schoolYearId: SchoolYearId) {
        repository.activate(schoolYearId)
    }
}
