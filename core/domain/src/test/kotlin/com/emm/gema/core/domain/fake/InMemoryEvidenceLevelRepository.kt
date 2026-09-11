package com.emm.gema.core.domain.fake

import com.emm.gema.core.domain.activity.Activity
import com.emm.gema.core.domain.activity.EvidenceLevel
import com.emm.gema.core.domain.activity.EvidenceLevelKey
import com.emm.gema.core.domain.activity.EvidenceLevelRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class InMemoryEvidenceLevelRepository(
    private val activityRepository: InMemoryActivityRepository,
) : EvidenceLevelRepository {

    private val levels: MutableStateFlow<List<EvidenceLevel>> = MutableStateFlow(emptyList())

    override fun observeByActivity(activityId: String): Flow<List<EvidenceLevel>> = levels
        .map { stored -> stored.filter { it.key.activityId == activityId } }

    override fun observeRecordedStudentCountsByPeriod(sectionId: String, periodId: String): Flow<Map<String, Int>> =
        combine(levels, activityRepository.observeByPeriod(sectionId, periodId)) { stored, activities ->
            val activityIds: Set<String> = activities.map(Activity::id).toSet()
            stored
                .filter { it.key.activityId in activityIds }
                .groupBy { it.key.activityId }
                .mapValues { (_, recorded) -> recorded.map { it.key.studentId }.distinct().size }
        }

    override suspend fun save(evidenceLevel: EvidenceLevel) {
        levels.value = levels.value.filterNot { it.key == evidenceLevel.key } + evidenceLevel
    }

    override suspend fun delete(key: EvidenceLevelKey) {
        levels.value = levels.value.filterNot { it.key == key }
    }

    override suspend fun deleteByActivity(activityId: String) {
        levels.value = levels.value.filterNot { it.key.activityId == activityId }
    }

    override suspend fun clearSection(sectionId: String) {
        val sectionActivityIds: Set<String> = activityRepository.findAllBySection(sectionId).map(Activity::id).toSet()
        levels.value = levels.value.filterNot { it.key.activityId in sectionActivityIds }
    }
}
