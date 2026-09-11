package com.emm.gema.core.domain.schoolyear

class GetSchoolYearUseCase(
    private val repository: SchoolYearRepository,
) {

    suspend operator fun invoke(schoolYearId: String): SchoolYear? = repository.findById(schoolYearId)
}
