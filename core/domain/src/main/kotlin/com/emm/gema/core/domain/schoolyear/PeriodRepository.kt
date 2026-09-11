package com.emm.gema.core.domain.schoolyear

import kotlinx.coroutines.flow.Flow

interface PeriodRepository {

    fun observeBySchoolYear(schoolYearId: SchoolYearId): Flow<List<Period>>

    suspend fun findBySchoolYear(schoolYearId: SchoolYearId): List<Period>

    suspend fun findById(id: PeriodId): Period?

    suspend fun saveAll(periods: List<Period>)
}
