package com.emm.gema.feature.sections

import com.emm.gema.core.domain.curriculum.WorkedCompetencyRepository
import com.emm.gema.core.domain.section.Area
import com.emm.gema.core.domain.section.Section
import com.emm.gema.core.domain.section.SectionAreaRepository
import com.emm.gema.core.domain.section.SectionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeSectionRepository(initial: List<Section> = emptyList()) : SectionRepository {

    val sections: MutableStateFlow<List<Section>> = MutableStateFlow(initial)
    var failsOnce: Boolean = false

    override fun observeBySchoolYear(schoolYearId: String): Flow<List<Section>> = sections
        .map { stored -> stored.filter { it.schoolYearId == schoolYearId } }

    override fun observeCountsBySchoolYear(): Flow<Map<String, Int>> = sections
        .map { stored -> stored.groupingBy { it.schoolYearId }.eachCount() }

    override suspend fun findById(id: String): Section? = sections.value.find { it.id == id }

    override suspend fun save(section: Section) {
        failOnce()
        sections.value = sections.value.filterNot { it.id == section.id } + section
    }

    override suspend fun delete(id: String) {
        failOnce()
        sections.value = sections.value.filterNot { it.id == id }
    }

    private fun failOnce() {
        if (!failsOnce) return
        failsOnce = false
        error("the disk is full")
    }
}

class FakeSectionAreaRepository : SectionAreaRepository {

    val hiddenAreas: MutableStateFlow<Map<String, Set<Area>>> = MutableStateFlow(emptyMap())

    override fun observeHiddenAreas(sectionId: String): Flow<Set<Area>> = hiddenAreas
        .map { stored -> stored[sectionId].orEmpty() }

    override suspend fun setAreaHidden(sectionId: String, area: Area, isHidden: Boolean) {
        val current: Set<Area> = hiddenAreas.value[sectionId].orEmpty()
        hiddenAreas.value = hiddenAreas.value + (sectionId to if (isHidden) current + area else current - area)
    }

    override suspend fun clearSection(sectionId: String) {
        hiddenAreas.value = hiddenAreas.value - sectionId
    }
}

class FakeWorkedCompetencyRepository : WorkedCompetencyRepository {

    val rows: MutableStateFlow<Set<Triple<String, String, String>>> = MutableStateFlow(emptySet())

    override fun observeWorked(sectionId: String, periodId: String): Flow<Set<String>> = rows.map { current ->
        current.filter { it.first == sectionId && it.second == periodId }.mapTo(mutableSetOf()) { it.third }
    }

    override suspend fun setWorked(
        sectionId: String,
        periodId: String,
        competencyId: String,
        isWorked: Boolean,
    ) {
        val row: Triple<String, String, String> = Triple(sectionId, periodId, competencyId)
        rows.value = if (isWorked) rows.value + row else rows.value - row
    }

    override suspend fun clearSection(sectionId: String) {
        rows.value = rows.value.filterNotTo(mutableSetOf()) { it.first == sectionId }
    }
}
