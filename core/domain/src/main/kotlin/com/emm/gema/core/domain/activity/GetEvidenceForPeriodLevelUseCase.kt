package com.emm.gema.core.domain.activity

import kotlinx.coroutines.flow.Flow

class GetEvidenceForPeriodLevelUseCase(
    private val repository: EvidenceLevelRepository,
) {

    operator fun invoke(
        sectionId: String,
        periodId: String,
        studentId: String,
        competencyId: String,
    ): Flow<List<EvidenceRecord>> = repository.observeForStudentAndCompetency(
        sectionId = sectionId,
        periodId = periodId,
        studentId = studentId,
        competencyId = competencyId,
    )
}
