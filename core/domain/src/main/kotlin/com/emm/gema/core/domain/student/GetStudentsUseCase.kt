package com.emm.gema.core.domain.student

import com.emm.gema.core.domain.section.SectionId
import kotlinx.coroutines.flow.Flow

class GetStudentsUseCase(
    private val repository: StudentRepository,
) {

    operator fun invoke(sectionId: SectionId): Flow<List<Student>> = repository.observeBySection(sectionId)
}
