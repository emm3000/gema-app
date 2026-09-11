package com.emm.gema.core.domain.activity

class RecordEvidenceLevelUseCase(
    private val repository: EvidenceLevelRepository,
) {

    suspend operator fun invoke(key: EvidenceLevelKey, mark: EvidenceMark?) {
        when (mark) {
            null -> repository.delete(key)
            EvidenceMark.NoEvidence -> repository.save(EvidenceLevel(key, achievementLevel = null))
            is EvidenceMark.Level -> repository.save(EvidenceLevel(key, achievementLevel = mark.achievementLevel))
        }
    }
}
