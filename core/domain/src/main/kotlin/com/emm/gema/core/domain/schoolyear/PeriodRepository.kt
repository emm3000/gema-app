package com.emm.gema.core.domain.schoolyear

import kotlinx.coroutines.flow.Flow

interface PeriodRepository {

    fun observeBySchoolYear(schoolYearId: String): Flow<List<Period>>

    suspend fun findBySchoolYear(schoolYearId: String): List<Period>

    suspend fun findById(id: String): Period?

    suspend fun saveAll(periods: List<Period>)
}
