package com.emm.gema.core.domain.activity

import kotlinx.coroutines.flow.Flow

class GetEvidenceForActivityUseCase(
    private val repository: EvidenceLevelRepository,
) {

    operator fun invoke(activityId: String): Flow<List<EvidenceLevel>> = repository.observeByActivity(activityId)
}
