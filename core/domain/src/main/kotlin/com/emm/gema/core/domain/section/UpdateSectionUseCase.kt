package com.emm.gema.core.domain.section

class UpdateSectionUseCase(
    private val repository: SectionRepository,
) {

    suspend operator fun invoke(sectionId: SectionId, grade: Grade, name: String) {
        val section: Section = requireNotNull(repository.findById(sectionId)) {
            "There is no section ${sectionId.value}"
        }
        repository.save(section.copy(grade = grade, name = name.trim()))
    }
}
