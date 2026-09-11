package com.emm.gema.core.domain.section

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetSectionAreasUseCase(
    private val repository: SectionAreaRepository,
) {

    operator fun invoke(sectionId: SectionId): Flow<List<SectionArea>> = repository
        .observeHiddenAreas(sectionId)
        .map { hidden -> Area.entries.map { SectionArea(area = it, isActive = it !in hidden) } }
}
