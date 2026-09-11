package com.emm.gema.core.domain.section

import kotlinx.coroutines.flow.Flow

class GetSectionCountsUseCase(
    private val repository: SectionRepository,
) {

    operator fun invoke(): Flow<Map<String, Int>> = repository.observeCountsBySchoolYear()
}
