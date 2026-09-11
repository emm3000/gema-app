package com.emm.gema.core.domain.evaluation

import com.emm.gema.core.domain.curriculum.CompetencyId
import com.emm.gema.core.domain.schoolyear.PeriodId
import com.emm.gema.core.domain.section.SectionId
import kotlinx.coroutines.flow.Flow

class GetRecordedLevelCountsUseCase(
    private val repository: PeriodLevelRepository,
) {

    operator fun invoke(sectionId: SectionId, periodId: PeriodId): Flow<Map<CompetencyId, Int>> =
        repository.observeRecordedCountsByPeriod(sectionId = sectionId, periodId = periodId)
}
