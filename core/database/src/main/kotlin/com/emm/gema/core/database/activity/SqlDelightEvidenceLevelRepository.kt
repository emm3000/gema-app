package com.emm.gema.core.database.activity

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.emm.gema.core.database.EvidenceLevelQueries
import com.emm.gema.core.database.GemaDb
import com.emm.gema.core.database.SelectRecordedStudentCountsByPeriod
import com.emm.gema.core.domain.activity.EvidenceLevel
import com.emm.gema.core.domain.activity.EvidenceLevelKey
import com.emm.gema.core.domain.activity.EvidenceLevelRepository
import com.emm.gema.core.domain.activity.EvidenceRecord
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

    override fun observeByActivity(activityId: String): Flow<List<EvidenceLevel>> = queries
        .selectByActivity(activityId)
        .asFlow()
        .mapToList(dispatcher)
        .map { rows -> rows.map { it.toDomain() } }

    override fun observeRecordedStudentCountsByPeriod(sectionId: String, periodId: String): Flow<Map<String, Int>> =
        queries.selectRecordedStudentCountsByPeriod(section_id = sectionId, period_id = periodId)
            .asFlow()
            .mapToList(dispatcher)
            .map { rows: List<SelectRecordedStudentCountsByPeriod> ->
                rows.associate { it.activity_id to it.student_count.toInt() }
            }

    override fun observeForStudentAndCompetency(
        sectionId: String,
        periodId: String,
        studentId: String,
        competencyId: String,
    ): Flow<List<EvidenceRecord>> = queries
        .selectForStudentAndCompetency(
            section_id = sectionId,
            period_id = periodId,
            student_id = studentId,
            competency_id = competencyId,
        )
        .asFlow()
        .mapToList(dispatcher)
        .map { rows -> rows.map { it.toDomain() } }

    override suspend fun save(evidenceLevel: EvidenceLevel): Unit = withContext(dispatcher) {
        queries.upsert(
            activity_id = evidenceLevel.key.activityId,
            student_id = evidenceLevel.key.studentId,
            competency_id = evidenceLevel.key.competencyId,
            achievement_level = evidenceLevel.achievementLevel.name,
        )
    }

    override suspend fun delete(key: EvidenceLevelKey): Unit = withContext(dispatcher) {
        queries.delete(activity_id = key.activityId, student_id = key.studentId, competency_id = key.competencyId)
    }

    override suspend fun deleteByActivity(activityId: String): Unit = withContext(dispatcher) {
        queries.deleteByActivity(activityId)
    }

    override suspend fun clearSection(sectionId: String): Unit = withContext(dispatcher) {
        queries.deleteBySection(sectionId)
    }
}
