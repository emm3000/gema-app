package com.emm.gema.core.domain.curriculum

import com.emm.gema.core.domain.section.Area
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class GetPeriodCompetenciesUseCase(
    private val competencyRepository: CompetencyRepository,
    private val workedCompetencyRepository: WorkedCompetencyRepository,
) {

    operator fun invoke(sectionId: String, periodId: String, area: Area): Flow<List<PeriodCompetency>> = flow {
        val competencies: List<Competency> = competencyRepository.findByArea(area)

        emitAll(
            workedCompetencyRepository.observeWorked(sectionId = sectionId, periodId = periodId)
                .map { worked -> competencies.map { PeriodCompetency(it, it.id in worked) } },
        )
    }
}
