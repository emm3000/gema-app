package com.emm.gema.core.domain.curriculum

import com.emm.gema.core.domain.schoolyear.PeriodId
import com.emm.gema.core.domain.section.Area
import com.emm.gema.core.domain.section.SectionAreaRepository
import com.emm.gema.core.domain.section.SectionId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

class GetWorkedCompetenciesUseCase(
    private val competencyRepository: CompetencyRepository,
    private val workedCompetencyRepository: WorkedCompetencyRepository,
    private val sectionAreaRepository: SectionAreaRepository,
) {

    operator fun invoke(sectionId: SectionId, periodId: PeriodId): Flow<List<Competency>> = flow {
        val competencies: List<Competency> = Area.entries.flatMap { competencyRepository.findByArea(it) }

        emitAll(
            combine(
                workedCompetencyRepository.observeWorked(sectionId = sectionId, periodId = periodId),
                sectionAreaRepository.observeHiddenAreas(sectionId),
            ) { worked: Set<CompetencyId>, hidden: Set<Area> ->
                competencies.filter { it.id in worked && it.area !in hidden }
            },
        )
    }
}
