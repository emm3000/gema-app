package com.emm.gema.core.domain.schoolyear

import kotlinx.coroutines.flow.Flow

interface SchoolYearRepository {

    fun observeAll(): Flow<List<SchoolYear>>

    suspend fun findById(id: SchoolYearId): SchoolYear?

    suspend fun save(schoolYear: SchoolYear)
}
