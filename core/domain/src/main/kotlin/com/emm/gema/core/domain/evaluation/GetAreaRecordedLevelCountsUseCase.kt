package com.emm.gema.core.domain.evaluation

import com.emm.gema.core.domain.curriculum.Competency
import com.emm.gema.core.domain.section.Area
import com.emm.gema.core.domain.section.SectionId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetAreaRecordedLevelCountsUseCase(
    private val repository: PeriodLevelRepository,
) {

    operator fun invoke(sectionId: SectionId): Flow<Map<Area, Int>> = repository
        .observeRecordedCountsBySection(sectionId)
        .map { counts ->
            counts.entries
                .mapNotNull { entry -> Competency.areaOf(entry.key)?.let { it to entry.value } }
                .groupBy({ it.first }, { it.second })
                .mapValues { entry -> entry.value.sum() }
        }
}
