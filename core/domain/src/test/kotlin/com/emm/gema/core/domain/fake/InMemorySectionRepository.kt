package com.emm.gema.core.domain.fake

import com.emm.gema.core.domain.section.Section
import com.emm.gema.core.domain.section.SectionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class InMemorySectionRepository : SectionRepository {

    private val sections: MutableStateFlow<List<Section>> = MutableStateFlow(emptyList())

    override fun observeBySchoolYear(schoolYearId: String): Flow<List<Section>> = sections
        .map { stored ->
            stored.filter { it.schoolYearId == schoolYearId }
                .sortedWith(compareBy({ it.grade.number }, { it.name }))
        }

    override fun observeCountsBySchoolYear(): Flow<Map<String, Int>> = sections
        .map { stored -> stored.groupingBy { it.schoolYearId }.eachCount() }

    override suspend fun findById(id: String): Section? = sections.value.find { it.id == id }

    override suspend fun save(section: Section) {
        sections.value = sections.value.filterNot { it.id == section.id } + section
    }

    override suspend fun delete(id: String) {
        sections.value = sections.value.filterNot { it.id == id }
    }
}
