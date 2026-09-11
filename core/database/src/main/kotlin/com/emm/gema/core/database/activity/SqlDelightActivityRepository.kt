package com.emm.gema.core.database.activity

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.emm.gema.core.database.ActivityQueries
import com.emm.gema.core.database.GemaDb
import com.emm.gema.core.domain.activity.Activity
import com.emm.gema.core.domain.activity.ActivityId
import com.emm.gema.core.domain.activity.ActivityRepository
import com.emm.gema.core.domain.schoolyear.PeriodId
import com.emm.gema.core.domain.section.SectionId
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SqlDelightActivityRepository(
    private val database: GemaDb,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ActivityRepository {

    private val queries: ActivityQueries = database.activityQueries

    override fun observeByPeriod(sectionId: SectionId, periodId: PeriodId): Flow<List<Activity>> = queries
        .selectByPeriod(section_id = sectionId.value, period_id = periodId.value)
        .asFlow()
        .mapToList(dispatcher)
        .map { rows -> rows.map { it.toDomain() } }

    override suspend fun findById(id: ActivityId): Activity? = withContext(dispatcher) {
        queries.selectById(id.value).executeAsOneOrNull()?.toDomain()
    }

    override suspend fun save(activity: Activity): Unit = withContext(dispatcher) {
        database.transaction {
            queries.upsert(
                id = activity.id.value,
                section_id = activity.sectionId.value,
                period_id = activity.periodId.value,
                name = activity.name,
                date = activity.date.toString(),
            )
            queries.deleteCompetenciesByActivity(activity.id.value)
            activity.competencyIds.forEach { competencyId ->
                queries.insertCompetency(activity_id = activity.id.value, competency_id = competencyId.value)
            }
        }
    }

    override suspend fun delete(id: ActivityId): Unit = withContext(dispatcher) {
        database.transaction {
            queries.deleteCompetenciesByActivity(id.value)
            queries.delete(id.value)
        }
    }

    override suspend fun clearSection(sectionId: SectionId): Unit = withContext(dispatcher) {
        database.transaction {
            queries.deleteCompetenciesBySection(sectionId.value)
            queries.deleteBySection(sectionId.value)
        }
    }
}
