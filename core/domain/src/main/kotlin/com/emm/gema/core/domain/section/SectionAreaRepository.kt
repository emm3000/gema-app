package com.emm.gema.core.domain.section

import kotlinx.coroutines.flow.Flow

interface SectionAreaRepository {

    fun observeHiddenAreas(sectionId: SectionId): Flow<Set<Area>>

    suspend fun setAreaHidden(sectionId: SectionId, area: Area, isHidden: Boolean)

    suspend fun clearSection(sectionId: SectionId)
}
