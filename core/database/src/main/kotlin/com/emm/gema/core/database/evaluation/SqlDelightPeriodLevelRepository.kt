package com.emm.gema.core.database.evaluation

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.emm.gema.core.database.GemaDb
import com.emm.gema.core.database.PeriodLevelQueries
import com.emm.gema.core.database.SelectRecordedCountsByPeriod
import com.emm.gema.core.database.SelectRecordedCountsBySection
import com.emm.gema.core.domain.curriculum.CompetencyId
import com.emm.gema.core.domain.evaluation.PeriodLevel
import com.emm.gema.core.domain.evaluation.PeriodLevelKey
import com.emm.gema.core.domain.evaluation.PeriodLevelRepository
import com.emm.gema.core.domain.schoolyear.PeriodId
import com.emm.gema.core.domain.section.SectionId
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

    override fun observeByPeriod(sectionId: SectionId, periodId: PeriodId): Flow<List<PeriodLevel>> = queries
        .selectByPeriod(section_id = sectionId.value, period_id = periodId.value)
        .asFlow()
        .mapToList(dispatcher)
        .map { rows -> rows.map { it.toDomain() } }

    override fun observeRecordedCountsByPeriod(
        sectionId: SectionId,
        periodId: PeriodId,
    ): Flow<Map<CompetencyId, Int>> = queries
        .selectRecordedCountsByPeriod(section_id = sectionId.value, period_id = periodId.value)
        .asFlow()
        .mapToList(dispatcher)
        .map { rows: List<SelectRecordedCountsByPeriod> ->
            rows.associate { CompetencyId(it.competency_id) to it.recorded_count.toInt() }
        }

    override fun observeRecordedCountsBySection(sectionId: SectionId): Flow<Map<CompetencyId, Int>> = queries
        .selectRecordedCountsBySection(sectionId.value)
        .asFlow()
        .mapToList(dispatcher)
        .map { rows: List<SelectRecordedCountsBySection> ->
            rows.associate { CompetencyId(it.competency_id) to it.recorded_count.toInt() }
        }

    override suspend fun find(key: PeriodLevelKey): PeriodLevel? = withContext(dispatcher) {
        queries.selectOne(
            section_id = key.sectionId.value,
            period_id = key.periodId.value,
            student_id = key.studentId.value,
            competency_id = key.competencyId.value,
        ).executeAsOneOrNull()?.toDomain()
    }

    override suspend fun save(periodLevel: PeriodLevel): Unit = withContext(dispatcher) {
        queries.upsert(
            section_id = periodLevel.key.sectionId.value,
            period_id = periodLevel.key.periodId.value,
            student_id = periodLevel.key.studentId.value,
            competency_id = periodLevel.key.competencyId.value,
            achievement_level = periodLevel.achievementLevel?.name,
            unworked_comment = periodLevel.unworkedComment?.name,
            descriptive_conclusion = periodLevel.descriptiveConclusion,
        )
    }

    override suspend fun delete(key: PeriodLevelKey): Unit = withContext(dispatcher) {
        queries.delete(
            section_id = key.sectionId.value,
            period_id = key.periodId.value,
            student_id = key.studentId.value,
            competency_id = key.competencyId.value,
        )
    }

    override suspend fun clearSection(sectionId: SectionId): Unit = withContext(dispatcher) {
        queries.deleteBySection(sectionId.value)
    }
}
