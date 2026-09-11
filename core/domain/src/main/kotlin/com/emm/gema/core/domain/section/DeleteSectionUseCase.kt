package com.emm.gema.core.domain.section

class DeleteSectionUseCase(
    private val sectionRepository: SectionRepository,
    private val sectionAreaRepository: SectionAreaRepository,
) {

    suspend operator fun invoke(sectionId: String) {
        sectionRepository.delete(sectionId)
        sectionAreaRepository.clearSection(sectionId)
    }
}
