package com.emm.gema.core.domain.section

import kotlinx.coroutines.flow.Flow

interface SectionRepository {

    fun observeBySchoolYear(schoolYearId: String): Flow<List<Section>>

    suspend fun findById(id: String): Section?

    suspend fun save(section: Section)

    suspend fun delete(id: String)
}
