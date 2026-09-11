package com.emm.gema.core.domain.student

import kotlinx.coroutines.flow.Flow

class GetStudentCountsUseCase(
    private val repository: StudentRepository,
) {

    operator fun invoke(): Flow<Map<String, Int>> = repository.observeCountsBySection()
}
