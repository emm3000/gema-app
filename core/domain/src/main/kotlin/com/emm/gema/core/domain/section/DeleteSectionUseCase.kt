package com.emm.gema.core.domain.section

import com.emm.gema.core.domain.curriculum.WorkedCompetencyRepository

class DeleteSectionUseCase(
    private val sectionRepository: SectionRepository,
    private val sectionAreaRepository: SectionAreaRepository,
    private val workedCompetencyRepository: WorkedCompetencyRepository,
) {

    suspend operator fun invoke(sectionId: String) {
        sectionRepository.delete(sectionId)
        sectionAreaRepository.clearSection(sectionId)
        workedCompetencyRepository.clearSection(sectionId)
    }
}
