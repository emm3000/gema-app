package com.emm.gema.core.database.evaluation

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.emm.gema.core.database.GemaDb
import com.emm.gema.core.database.PeriodLevelQueries
import com.emm.gema.core.database.SelectRecordedCountsByPeriod
import com.emm.gema.core.database.SelectRecordedCountsBySection
import com.emm.gema.core.domain.evaluation.PeriodLevel
import com.emm.gema.core.domain.evaluation.PeriodLevelKey
import com.emm.gema.core.domain.evaluation.PeriodLevelRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SqlDelightPeriodLevelRepository(
    database: GemaDb,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : PeriodLevelRepository {

    private val queries: PeriodLevelQueries = database.periodLevelQueries

    override fun observeByPeriod(sectionId: String, periodId: String): Flow<List<PeriodLevel>> = queries
        .selectByPeriod(section_id = sectionId, period_id = periodId)
        .asFlow()
        .mapToList(dispatcher)
        .map { rows -> rows.map { it.toDomain() } }

    override fun observeRecordedCountsByPeriod(sectionId: String, periodId: String): Flow<Map<String, Int>> = queries
        .selectRecordedCountsByPeriod(section_id = sectionId, period_id = periodId)
        .asFlow()
        .mapToList(dispatcher)
        .map { rows: List<SelectRecordedCountsByPeriod> ->
            rows.associate { it.competency_id to it.recorded_count.toInt() }
        }

    override fun observeRecordedCountsBySection(sectionId: String): Flow<Map<String, Int>> = queries
        .selectRecordedCountsBySection(sectionId)
        .asFlow()
        .mapToList(dispatcher)
        .map { rows: List<SelectRecordedCountsBySection> ->
            rows.associate { it.competency_id to it.recorded_count.toInt() }
        }

    override suspend fun find(key: PeriodLevelKey): PeriodLevel? = withContext(dispatcher) {
        queries.selectOne(
            section_id = key.sectionId,
            period_id = key.periodId,
            student_id = key.studentId,
            competency_id = key.competencyId,
        ).executeAsOneOrNull()?.toDomain()
    }

    override suspend fun save(periodLevel: PeriodLevel): Unit = withContext(dispatcher) {
        queries.upsert(
            section_id = periodLevel.key.sectionId,
            period_id = periodLevel.key.periodId,
            student_id = periodLevel.key.studentId,
            competency_id = periodLevel.key.competencyId,
            achievement_level = periodLevel.achievementLevel?.name,
            unworked_comment = periodLevel.unworkedComment?.name,
            descriptive_conclusion = periodLevel.descriptiveConclusion,
        )
    }

    override suspend fun delete(key: PeriodLevelKey): Unit = withContext(dispatcher) {
        queries.delete(
            section_id = key.sectionId,
            period_id = key.periodId,
            student_id = key.studentId,
            competency_id = key.competencyId,
        )
    }

    override suspend fun clearSection(sectionId: String): Unit = withContext(dispatcher) {
        queries.deleteBySection(sectionId)
    }
}
