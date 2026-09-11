package com.emm.gema.core.domain.activity

import com.emm.gema.core.domain.schoolyear.PeriodId
import com.emm.gema.core.domain.section.SectionId
import kotlinx.coroutines.flow.Flow

interface ActivityRepository {

    fun observeByPeriod(sectionId: SectionId, periodId: PeriodId): Flow<List<Activity>>

    suspend fun findById(id: ActivityId): Activity?

    suspend fun save(activity: Activity)

    suspend fun delete(id: ActivityId)

    suspend fun clearSection(sectionId: SectionId)
}
