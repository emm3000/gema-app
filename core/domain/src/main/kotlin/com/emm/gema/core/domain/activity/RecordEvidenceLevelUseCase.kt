package com.emm.gema.core.domain.activity

import com.emm.gema.core.domain.evaluation.AchievementLevel

class RecordEvidenceLevelUseCase(
    private val repository: EvidenceLevelRepository,
) {

    suspend operator fun invoke(key: EvidenceLevelKey, level: AchievementLevel?) {
        if (level == null) {
            repository.delete(key)
        } else {
            repository.save(EvidenceLevel(key, level))
        }
    }
}
