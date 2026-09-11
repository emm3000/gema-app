package com.emm.gema.core.domain.activity

import com.emm.gema.core.domain.curriculum.CompetencyId
import com.emm.gema.core.domain.schoolyear.PeriodId
import com.emm.gema.core.domain.section.SectionId
import com.emm.gema.core.domain.student.StudentId
import kotlinx.coroutines.flow.Flow

class GetEvidenceForPeriodLevelUseCase(
    private val repository: EvidenceLevelRepository,
) {

    operator fun invoke(
        sectionId: SectionId,
        periodId: PeriodId,
        studentId: StudentId,
        competencyId: CompetencyId,
    ): Flow<List<EvidenceRecord>> = repository.observeForStudentAndCompetency(
        sectionId = sectionId,
        periodId = periodId,
        studentId = studentId,
        competencyId = competencyId,
    )
}
