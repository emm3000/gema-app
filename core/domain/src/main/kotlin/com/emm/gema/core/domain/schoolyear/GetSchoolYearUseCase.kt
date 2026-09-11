package com.emm.gema.core.domain.schoolyear

class GetSchoolYearUseCase(
    private val repository: SchoolYearRepository,
) {

    suspend operator fun invoke(schoolYearId: SchoolYearId): SchoolYear? = repository.findById(schoolYearId)
}
