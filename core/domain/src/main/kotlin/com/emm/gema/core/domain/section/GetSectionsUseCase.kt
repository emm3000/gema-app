package com.emm.gema.core.domain.section

import com.emm.gema.core.domain.schoolyear.SchoolYearId
import kotlinx.coroutines.flow.Flow

class GetSectionsUseCase(
    private val repository: SectionRepository,
) {

    operator fun invoke(schoolYearId: SchoolYearId): Flow<List<Section>> =
        repository.observeBySchoolYear(schoolYearId)
}
