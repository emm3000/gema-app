package com.emm.gema.core.domain.section

class DeleteSectionUseCase(
    private val sectionCascade: SectionCascade,
) {

    suspend operator fun invoke(sectionId: String) {
        sectionCascade.deleteSection(sectionId)
    }
}
