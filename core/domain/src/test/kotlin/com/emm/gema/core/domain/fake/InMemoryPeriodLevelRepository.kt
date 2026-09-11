package com.emm.gema.core.domain.fake

import com.emm.gema.core.domain.curriculum.CompetencyId
import com.emm.gema.core.domain.evaluation.PeriodLevel
import com.emm.gema.core.domain.evaluation.PeriodLevelKey
import com.emm.gema.core.domain.evaluation.PeriodLevelRepository
import com.emm.gema.core.domain.schoolyear.PeriodId
import com.emm.gema.core.domain.section.SectionId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class InMemoryPeriodLevelRepository : PeriodLevelRepository {

    private val levels: MutableStateFlow<List<PeriodLevel>> = MutableStateFlow(emptyList())

    override fun observeByPeriod(sectionId: SectionId, periodId: PeriodId): Flow<List<PeriodLevel>> = levels
        .map { stored -> stored.filter { it.key.sectionId == sectionId && it.key.periodId == periodId } }

    override fun observeRecordedCountsByPeriod(
        sectionId: SectionId,
        periodId: PeriodId,
    ): Flow<Map<CompetencyId, Int>> = levels
        .map { stored ->
            stored
                .filter { it.key.sectionId == sectionId && it.key.periodId == periodId && it.isRecorded }
                .groupingBy { it.key.competencyId }
                .eachCount()
        }

    override fun observeRecordedCountsBySection(sectionId: SectionId): Flow<Map<CompetencyId, Int>> = levels
        .map { stored ->
            stored
                .filter { it.key.sectionId == sectionId && it.isRecorded }
                .groupingBy { it.key.competencyId }
                .eachCount()
        }

    override suspend fun find(key: PeriodLevelKey): PeriodLevel? = levels.value.find { it.key == key }

    override suspend fun save(periodLevel: PeriodLevel) {
        levels.value = levels.value.filterNot { it.key == periodLevel.key } + periodLevel
    }

    override suspend fun delete(key: PeriodLevelKey) {
        levels.value = levels.value.filterNot { it.key == key }
    }

    override suspend fun clearSection(sectionId: SectionId) {
        levels.value = levels.value.filterNot { it.key.sectionId == sectionId }
    }
}
