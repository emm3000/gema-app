package com.emm.gema.core.domain.schoolyear

class SwitchSchoolYearUseCase(
    private val repository: ActiveSchoolYearRepository,
) {

    suspend operator fun invoke(schoolYearId: String) {
        repository.activate(schoolYearId)
    }
}
