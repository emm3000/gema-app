package com.emm.gema.core.domain.curriculum

class SeedCurriculumUseCase(
    private val repository: CompetencyRepository,
) {

    suspend operator fun invoke() {
        repository.seed(
            competencies = PrimaryCurriculum.competencies,
            curriculumVersion = PrimaryCurriculum.VERSION,
        )
    }
}
