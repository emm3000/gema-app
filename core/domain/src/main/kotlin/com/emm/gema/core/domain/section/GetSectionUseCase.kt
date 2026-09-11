package com.emm.gema.core.domain.section

class GetSectionUseCase(
    private val repository: SectionRepository,
) {

    suspend operator fun invoke(sectionId: SectionId): Section? = repository.findById(sectionId)
}
