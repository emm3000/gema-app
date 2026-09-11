package com.emm.gema.core.domain.fake

import com.emm.gema.core.domain.activity.Activity
import com.emm.gema.core.domain.activity.ActivityId
import com.emm.gema.core.domain.activity.EvidenceLevel
import com.emm.gema.core.domain.activity.EvidenceLevelKey
import com.emm.gema.core.domain.activity.EvidenceLevelRepository
import com.emm.gema.core.domain.activity.EvidenceRecord
import com.emm.gema.core.domain.curriculum.CompetencyId
import com.emm.gema.core.domain.schoolyear.PeriodId
import com.emm.gema.core.domain.section.SectionId
import com.emm.gema.core.domain.student.StudentId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class InMemoryEvidenceLevelRepository(
    private val activityRepository: InMemoryActivityRepository,
) : EvidenceLevelRepository {

    private val levels: MutableStateFlow<List<EvidenceLevel>> = MutableStateFlow(emptyList())

    override fun observeByActivity(activityId: ActivityId): Flow<List<EvidenceLevel>> = levels
        .map { stored -> stored.filter { it.key.activityId == activityId } }

    override fun observeRecordedStudentCountsByPeriod(
        sectionId: SectionId,
        periodId: PeriodId,
    ): Flow<Map<ActivityId, Int>> =
        combine(levels, activityRepository.observeByPeriod(sectionId, periodId)) { stored, activities ->
            val activityIds: Set<ActivityId> = activities.map(Activity::id).toSet()
            stored
                .filter { it.key.activityId in activityIds }
                .groupBy { it.key.activityId }
                .mapValues { (_, recorded) -> recorded.map { it.key.studentId }.distinct().size }
        }

    override fun observeForStudentAndCompetency(
        sectionId: SectionId,
        periodId: PeriodId,
        studentId: StudentId,
        competencyId: CompetencyId,
    ): Flow<List<EvidenceRecord>> = combine(
        levels,
        activityRepository.observeByPeriod(sectionId, periodId),
    ) { stored, activities ->
        val activitiesById: Map<ActivityId, Activity> = activities.associateBy(Activity::id)

        stored
            .filter { it.key.studentId == studentId && it.key.competencyId == competencyId }
            .mapNotNull { level -> activitiesById[level.key.activityId]?.let { level to it } }
            .sortedBy { (_, activity) -> activity.date }
            .map { (level, activity) ->
                EvidenceRecord(
                    activityId = activity.id,
                    activityName = activity.name,
                    date = activity.date,
                    achievementLevel = level.achievementLevel,
                )
            }
    }

    override suspend fun save(evidenceLevel: EvidenceLevel) {
        levels.value = levels.value.filterNot { it.key == evidenceLevel.key } + evidenceLevel
    }

    override suspend fun delete(key: EvidenceLevelKey) {
        levels.value = levels.value.filterNot { it.key == key }
    }

    override suspend fun deleteByActivity(activityId: ActivityId) {
        levels.value = levels.value.filterNot { it.key.activityId == activityId }
    }

    override suspend fun clearSection(sectionId: SectionId) {
        val sectionActivityIds: Set<ActivityId> = activityRepository.findAllBySection(sectionId)
            .map(Activity::id)
            .toSet()
        levels.value = levels.value.filterNot { it.key.activityId in sectionActivityIds }
    }
}
