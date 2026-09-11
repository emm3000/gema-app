package com.emm.gema.core.database.activity

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.emm.gema.core.database.EvidenceLevelQueries
import com.emm.gema.core.database.GemaDb
import com.emm.gema.core.database.SelectRecordedStudentCountsByPeriod
import com.emm.gema.core.domain.activity.ActivityId
import com.emm.gema.core.domain.activity.EvidenceLevel
import com.emm.gema.core.domain.activity.EvidenceLevelKey
import com.emm.gema.core.domain.activity.EvidenceLevelRepository
import com.emm.gema.core.domain.activity.EvidenceRecord
import com.emm.gema.core.domain.curriculum.CompetencyId
import com.emm.gema.core.domain.schoolyear.PeriodId
import com.emm.gema.core.domain.section.SectionId
import com.emm.gema.core.domain.student.StudentId
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SqlDelightEvidenceLevelRepository(
    database: GemaDb,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : EvidenceLevelRepository {

    private val queries: EvidenceLevelQueries = database.evidenceLevelQueries

    override fun observeByActivity(activityId: ActivityId): Flow<List<EvidenceLevel>> = queries
        .selectByActivity(activityId.value)
        .asFlow()
        .mapToList(dispatcher)
        .map { rows -> rows.map { it.toDomain() } }

    override fun observeRecordedStudentCountsByPeriod(
        sectionId: SectionId,
        periodId: PeriodId,
    ): Flow<Map<ActivityId, Int>> =
        queries.selectRecordedStudentCountsByPeriod(section_id = sectionId.value, period_id = periodId.value)
            .asFlow()
            .mapToList(dispatcher)
            .map { rows: List<SelectRecordedStudentCountsByPeriod> ->
                rows.associate { ActivityId(it.activity_id) to it.student_count.toInt() }
            }

    override fun observeForStudentAndCompetency(
        sectionId: SectionId,
        periodId: PeriodId,
        studentId: StudentId,
        competencyId: CompetencyId,
    ): Flow<List<EvidenceRecord>> = queries
        .selectForStudentAndCompetency(
            section_id = sectionId.value,
            period_id = periodId.value,
            student_id = studentId.value,
            competency_id = competencyId.value,
        )
        .asFlow()
        .mapToList(dispatcher)
        .map { rows -> rows.map { it.toDomain() } }

    override suspend fun save(evidenceLevel: EvidenceLevel): Unit = withContext(dispatcher) {
        queries.upsert(
            activity_id = evidenceLevel.key.activityId.value,
            student_id = evidenceLevel.key.studentId.value,
            competency_id = evidenceLevel.key.competencyId.value,
            achievement_level = evidenceLevel.achievementLevel?.name,
        )
    }

    override suspend fun delete(key: EvidenceLevelKey): Unit = withContext(dispatcher) {
        queries.delete(
            activity_id = key.activityId.value,
            student_id = key.studentId.value,
            competency_id = key.competencyId.value,
        )
    }

    override suspend fun deleteByActivity(activityId: ActivityId): Unit = withContext(dispatcher) {
        queries.deleteByActivity(activityId.value)
    }

    override suspend fun clearSection(sectionId: SectionId): Unit = withContext(dispatcher) {
        queries.deleteBySection(sectionId.value)
    }
}
