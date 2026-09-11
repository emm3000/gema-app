package com.emm.gema.core.domain.section

import com.emm.gema.core.domain.id.IdGenerator
import com.emm.gema.core.domain.schoolyear.SchoolYearId

class CreateSectionUseCase(
    private val repository: SectionRepository,
    private val idGenerator: IdGenerator,
) {

    suspend operator fun invoke(schoolYearId: SchoolYearId, grade: Grade, name: String): Section {
        val section = Section(
            id = SectionId(idGenerator.newId()),
            schoolYearId = schoolYearId,
            grade = grade,
            name = name.trim(),
        )
        repository.save(section)
        return section
    }
}
