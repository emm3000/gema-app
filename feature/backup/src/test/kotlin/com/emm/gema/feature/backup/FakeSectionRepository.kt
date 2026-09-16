package com.emm.gema.feature.backup

import com.emm.gema.core.domain.schoolyear.SchoolYearId
import com.emm.gema.core.domain.section.Section
import com.emm.gema.core.domain.section.SectionId
import com.emm.gema.core.domain.section.SectionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeSectionRepository(
    vararg sections: Section,
) : SectionRepository {

    private val stored: MutableStateFlow<List<Section>> = MutableStateFlow(sections.toList())

    override fun observeBySchoolYear(schoolYearId: SchoolYearId): Flow<List<Section>> =
        MutableStateFlow(stored.value.filter { it.schoolYearId == schoolYearId })

    override fun observeCountsBySchoolYear(): Flow<Map<SchoolYearId, Int>> =
        throw UnsupportedOperationException("not needed by BackupViewModelTest")

    override suspend fun findById(id: SectionId): Section? = stored.value.find { it.id == id }

    override suspend fun save(section: Section) {
        stored.value = stored.value + section
    }

    override suspend fun delete(id: SectionId) {
        stored.value = stored.value.filterNot { it.id == id }
    }
}
