package com.emm.gema.core.domain.curriculum

import com.emm.gema.core.domain.schoolyear.PeriodId
import com.emm.gema.core.domain.section.SectionId
import kotlinx.coroutines.flow.Flow

interface WorkedCompetencyRepository {

    fun observeWorked(sectionId: SectionId, periodId: PeriodId): Flow<Set<CompetencyId>>

    suspend fun setWorked(sectionId: SectionId, periodId: PeriodId, competencyId: CompetencyId, isWorked: Boolean)

    suspend fun clearSection(sectionId: SectionId)
}
