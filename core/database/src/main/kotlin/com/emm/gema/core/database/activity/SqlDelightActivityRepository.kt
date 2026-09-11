package com.emm.gema.core.database.activity

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.emm.gema.core.database.ActivityQueries
import com.emm.gema.core.database.GemaDb
import com.emm.gema.core.domain.activity.Activity
import com.emm.gema.core.domain.activity.ActivityRepository
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

    override fun observeByPeriod(sectionId: String, periodId: String): Flow<List<Activity>> = queries
        .selectByPeriod(section_id = sectionId, period_id = periodId)
        .asFlow()
        .mapToList(dispatcher)
        .map { rows -> rows.map { it.toDomain() } }

    override suspend fun findById(id: String): Activity? = withContext(dispatcher) {
        queries.selectById(id).executeAsOneOrNull()?.toDomain()
    }

    override suspend fun save(activity: Activity): Unit = withContext(dispatcher) {
        database.transaction {
            queries.upsert(
                id = activity.id,
                section_id = activity.sectionId,
                period_id = activity.periodId,
                name = activity.name,
                date = activity.date.toString(),
            )
            queries.deleteCompetenciesByActivity(activity.id)
            activity.competencyIds.forEach { competencyId ->
                queries.insertCompetency(activity_id = activity.id, competency_id = competencyId)
            }
        }
    }

    override suspend fun delete(id: String): Unit = withContext(dispatcher) {
        database.transaction {
            queries.deleteCompetenciesByActivity(id)
            queries.delete(id)
        }
    }

    override suspend fun clearSection(sectionId: String): Unit = withContext(dispatcher) {
        database.transaction {
            queries.deleteCompetenciesBySection(sectionId)
            queries.deleteBySection(sectionId)
        }
    }
}
