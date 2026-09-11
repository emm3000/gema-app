package com.emm.gema.core.domain.fake

import com.emm.gema.core.domain.activity.Activity
import com.emm.gema.core.domain.activity.ActivityId
import com.emm.gema.core.domain.activity.ActivityRepository
import com.emm.gema.core.domain.schoolyear.PeriodId
import com.emm.gema.core.domain.section.SectionId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class InMemoryActivityRepository : ActivityRepository {

    private val activities: MutableStateFlow<List<Activity>> = MutableStateFlow(emptyList())

    override fun observeByPeriod(sectionId: SectionId, periodId: PeriodId): Flow<List<Activity>> = activities
        .map { stored ->
            stored.filter { it.sectionId == sectionId && it.periodId == periodId }.sortedByDescending { it.date }
        }

    override suspend fun findById(id: ActivityId): Activity? = activities.value.find { it.id == id }

    override suspend fun save(activity: Activity) {
        activities.value = activities.value.filterNot { it.id == activity.id } + activity
    }

    override suspend fun delete(id: ActivityId) {
        activities.value = activities.value.filterNot { it.id == id }
    }

    override suspend fun clearSection(sectionId: SectionId) {
        activities.value = activities.value.filterNot { it.sectionId == sectionId }
    }

    fun findAllBySection(sectionId: SectionId): List<Activity> = activities.value.filter { it.sectionId == sectionId }
}
