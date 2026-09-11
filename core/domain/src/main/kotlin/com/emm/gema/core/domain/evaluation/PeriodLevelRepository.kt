package com.emm.gema.core.domain.evaluation

import kotlinx.coroutines.flow.Flow

interface PeriodLevelRepository {

    fun observeByPeriod(sectionId: String, periodId: String): Flow<List<PeriodLevel>>

    fun observeRecordedCountsByPeriod(sectionId: String, periodId: String): Flow<Map<String, Int>>

    fun observeRecordedCountsBySection(sectionId: String): Flow<Map<String, Int>>

    suspend fun find(key: PeriodLevelKey): PeriodLevel?

    suspend fun save(periodLevel: PeriodLevel)

    suspend fun delete(key: PeriodLevelKey)

    suspend fun clearSection(sectionId: String)
}
