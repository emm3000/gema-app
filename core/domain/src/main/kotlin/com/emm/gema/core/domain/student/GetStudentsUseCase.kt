package com.emm.gema.core.domain.student

import kotlinx.coroutines.flow.Flow

class GetStudentsUseCase(
    private val repository: StudentRepository,
) {

    operator fun invoke(sectionId: String): Flow<List<Student>> = repository.observeBySection(sectionId)
}
