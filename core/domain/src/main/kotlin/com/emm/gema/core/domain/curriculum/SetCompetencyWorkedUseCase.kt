package com.emm.gema.core.domain.curriculum

class SetCompetencyWorkedUseCase(
    private val repository: WorkedCompetencyRepository,
) {

    suspend operator fun invoke(sectionId: String, periodId: String, competencyId: String, isWorked: Boolean) {
        repository.setWorked(
            sectionId = sectionId,
            periodId = periodId,
            competencyId = competencyId,
            isWorked = isWorked,
        )
    }
}
