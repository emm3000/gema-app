package com.emm.gema.core.domain.student

import com.emm.gema.core.domain.section.SectionId
import kotlinx.coroutines.flow.Flow

class GetStudentCountsUseCase(
    private val repository: StudentRepository,
) {

    operator fun invoke(): Flow<Map<SectionId, Int>> = repository.observeCountsBySection()
}
