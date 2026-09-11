package com.emm.gema.core.domain.section

import com.emm.gema.core.domain.schoolyear.SchoolYearId
import kotlinx.coroutines.flow.Flow

interface SectionRepository {

    fun observeBySchoolYear(schoolYearId: SchoolYearId): Flow<List<Section>>

    fun observeCountsBySchoolYear(): Flow<Map<SchoolYearId, Int>>

    suspend fun findById(id: SectionId): Section?

    suspend fun save(section: Section)

    suspend fun delete(id: SectionId)
}
