package com.emm.gema.core.domain.evaluation

import com.emm.gema.core.domain.curriculum.CompetencyId
import com.emm.gema.core.domain.schoolyear.PeriodId
import com.emm.gema.core.domain.section.SectionId
import kotlinx.coroutines.flow.Flow

interface PeriodLevelRepository {

    fun observeByPeriod(sectionId: SectionId, periodId: PeriodId): Flow<List<PeriodLevel>>

    fun observeRecordedCountsByPeriod(sectionId: SectionId, periodId: PeriodId): Flow<Map<CompetencyId, Int>>

    fun observeRecordedCountsBySection(sectionId: SectionId): Flow<Map<CompetencyId, Int>>

    suspend fun find(key: PeriodLevelKey): PeriodLevel?

    suspend fun save(periodLevel: PeriodLevel)

    suspend fun delete(key: PeriodLevelKey)

    suspend fun clearSection(sectionId: SectionId)
}
