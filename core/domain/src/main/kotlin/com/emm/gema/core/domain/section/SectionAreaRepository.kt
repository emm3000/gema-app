package com.emm.gema.core.domain.section

import kotlinx.coroutines.flow.Flow

interface SectionAreaRepository {

    fun observeHiddenAreas(sectionId: String): Flow<Set<Area>>

    suspend fun setAreaHidden(sectionId: String, area: Area, isHidden: Boolean)

    suspend fun clearSection(sectionId: String)
}
