package com.emm.gema.core.domain.activity

import com.emm.gema.core.domain.schoolyear.PeriodId
import com.emm.gema.core.domain.section.SectionId
import kotlinx.coroutines.flow.Flow

class GetActivitiesUseCase(
    private val repository: ActivityRepository,
) {

    operator fun invoke(sectionId: SectionId, periodId: PeriodId): Flow<List<Activity>> =
        repository.observeByPeriod(sectionId, periodId)
}
