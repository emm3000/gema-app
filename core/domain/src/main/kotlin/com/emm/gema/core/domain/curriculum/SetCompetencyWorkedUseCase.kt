package com.emm.gema.core.domain.curriculum

import com.emm.gema.core.domain.schoolyear.PeriodId
import com.emm.gema.core.domain.section.SectionId

class SetCompetencyWorkedUseCase(
    private val repository: WorkedCompetencyRepository,
) {

    suspend operator fun invoke(
        sectionId: SectionId,
        periodId: PeriodId,
        competencyId: CompetencyId,
        isWorked: Boolean,
    ) {
        repository.setWorked(
            sectionId = sectionId,
            periodId = periodId,
            competencyId = competencyId,
            isWorked = isWorked,
        )
    }
}
