package com.emm.gema.core.domain.activity

import com.emm.gema.core.domain.curriculum.CompetencyId
import com.emm.gema.core.domain.schoolyear.PeriodId
import com.emm.gema.core.domain.section.SectionId
import com.emm.gema.core.domain.student.StudentId
import kotlinx.coroutines.flow.Flow

interface EvidenceLevelRepository {

    fun observeByActivity(activityId: ActivityId): Flow<List<EvidenceLevel>>

    fun observeRecordedStudentCountsByPeriod(sectionId: SectionId, periodId: PeriodId): Flow<Map<ActivityId, Int>>

    fun observeForStudentAndCompetency(
        sectionId: SectionId,
        periodId: PeriodId,
        studentId: StudentId,
        competencyId: CompetencyId,
    ): Flow<List<EvidenceRecord>>

    suspend fun save(evidenceLevel: EvidenceLevel)

    suspend fun delete(key: EvidenceLevelKey)

    suspend fun deleteByActivity(activityId: ActivityId)

    suspend fun clearSection(sectionId: SectionId)
}
