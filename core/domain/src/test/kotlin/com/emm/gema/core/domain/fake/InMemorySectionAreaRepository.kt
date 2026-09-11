package com.emm.gema.core.domain.fake

import com.emm.gema.core.domain.section.Area
import com.emm.gema.core.domain.section.SectionAreaRepository
import com.emm.gema.core.domain.section.SectionId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class InMemorySectionAreaRepository : SectionAreaRepository {

    private val hiddenAreas: MutableStateFlow<Map<SectionId, Set<Area>>> = MutableStateFlow(emptyMap())

    override fun observeHiddenAreas(sectionId: SectionId): Flow<Set<Area>> = hiddenAreas
        .map { stored -> stored[sectionId].orEmpty() }

    override suspend fun setAreaHidden(sectionId: SectionId, area: Area, isHidden: Boolean) {
        val current: Set<Area> = hiddenAreas.value[sectionId].orEmpty()
        val updated: Set<Area> = if (isHidden) current + area else current - area
        hiddenAreas.value = hiddenAreas.value + (sectionId to updated)
    }

    override suspend fun clearSection(sectionId: SectionId) {
        hiddenAreas.value = hiddenAreas.value - sectionId
    }
}
