package com.emm.gema.core.domain.activity

import kotlinx.coroutines.flow.Flow

interface ActivityRepository {

    fun observeByPeriod(sectionId: String, periodId: String): Flow<List<Activity>>

    suspend fun findById(id: String): Activity?

    suspend fun save(activity: Activity)

    suspend fun delete(id: String)

    suspend fun clearSection(sectionId: String)
}
