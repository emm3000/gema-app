package com.emm.gema.core.domain.fake

import com.emm.gema.core.domain.curriculum.CompetencyId
import com.emm.gema.core.domain.curriculum.WorkedCompetencyRepository
import com.emm.gema.core.domain.schoolyear.PeriodId
import com.emm.gema.core.domain.section.SectionId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class InMemoryWorkedCompetencyRepository : WorkedCompetencyRepository {

    val rows: MutableStateFlow<Set<Triple<SectionId, PeriodId, CompetencyId>>> = MutableStateFlow(emptySet())

    override fun observeWorked(
        sectionId: SectionId,
        periodId: PeriodId,
    ): Flow<Set<CompetencyId>> = rows.map { current ->
        current.filter { it.first == sectionId && it.second == periodId }.mapTo(mutableSetOf()) { it.third }
    }

    override suspend fun setWorked(
        sectionId: SectionId,
        periodId: PeriodId,
        competencyId: CompetencyId,
        isWorked: Boolean,
    ) {
        val row: Triple<SectionId, PeriodId, CompetencyId> = Triple(sectionId, periodId, competencyId)
        rows.value = if (isWorked) rows.value + row else rows.value - row
    }

    override suspend fun clearSection(sectionId: SectionId) {
        rows.value = rows.value.filterNotTo(mutableSetOf()) { it.first == sectionId }
    }
}
