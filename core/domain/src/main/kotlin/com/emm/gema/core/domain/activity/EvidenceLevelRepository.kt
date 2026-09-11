package com.emm.gema.core.domain.activity

import kotlinx.coroutines.flow.Flow

interface EvidenceLevelRepository {

    fun observeByActivity(activityId: String): Flow<List<EvidenceLevel>>

    fun observeRecordedStudentCountsByPeriod(sectionId: String, periodId: String): Flow<Map<String, Int>>

    suspend fun save(evidenceLevel: EvidenceLevel)

    suspend fun delete(key: EvidenceLevelKey)

    suspend fun deleteByActivity(activityId: String)

    suspend fun clearSection(sectionId: String)
}
