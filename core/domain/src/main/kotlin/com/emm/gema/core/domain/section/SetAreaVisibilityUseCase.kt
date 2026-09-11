package com.emm.gema.core.domain.section

class SetAreaVisibilityUseCase(
    private val repository: SectionAreaRepository,
) {

    suspend operator fun invoke(sectionId: String, area: Area, isActive: Boolean) {
        repository.setAreaHidden(sectionId = sectionId, area = area, isHidden = !isActive)
    }
}
