package com.emm.gema.core.domain.section

import kotlinx.coroutines.flow.Flow

class GetSectionsUseCase(
    private val repository: SectionRepository,
) {

    operator fun invoke(schoolYearId: String): Flow<List<Section>> =
        repository.observeBySchoolYear(schoolYearId)
}
