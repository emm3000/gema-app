package com.emm.gema.core.domain.evaluation

import com.emm.gema.core.domain.activity.EvidenceLevelRepository
import com.emm.gema.core.domain.activity.EvidenceRecord
import kotlinx.coroutines.flow.first

data class PeriodLevelSheetContext(
    val periodLevel: PeriodLevel,
    val evidence: List<EvidenceRecord>,
)

class GetPeriodLevelSheetContextUseCase(
    private val periodLevelRepository: PeriodLevelRepository,
    private val evidenceLevelRepository: EvidenceLevelRepository,
) {

    suspend operator fun invoke(key: PeriodLevelKey): PeriodLevelSheetContext {
        val periodLevel: PeriodLevel = periodLevelRepository.find(key) ?: PeriodLevel(key)
        val evidence: List<EvidenceRecord> = evidenceLevelRepository.observeForStudentAndCompetency(
            sectionId = key.sectionId,
            periodId = key.periodId,
            studentId = key.studentId,
            competencyId = key.competencyId,
        ).first()

        return PeriodLevelSheetContext(periodLevel, evidence)
    }
}
