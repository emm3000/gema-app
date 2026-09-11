package com.emm.gema.core.database.curriculum

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.emm.gema.core.database.GemaDb
import com.emm.gema.core.database.WorkedCompetencyQueries
import com.emm.gema.core.domain.curriculum.WorkedCompetencyRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SqlDelightWorkedCompetencyRepository(
    database: GemaDb,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : WorkedCompetencyRepository {

    private val queries: WorkedCompetencyQueries = database.workedCompetencyQueries

    override fun observeWorked(sectionId: String, periodId: String): Flow<Set<String>> = queries
        .selectBySectionAndPeriod(section_id = sectionId, period_id = periodId)
        .asFlow()
        .mapToList(dispatcher)
        .map { rows -> rows.toSet() }

    override suspend fun setWorked(
        sectionId: String,
        periodId: String,
        competencyId: String,
        isWorked: Boolean,
    ): Unit = withContext(dispatcher) {
        if (isWorked) {
            queries.insert(section_id = sectionId, period_id = periodId, competency_id = competencyId)
        } else {
            queries.delete(section_id = sectionId, period_id = periodId, competency_id = competencyId)
        }
    }

    override suspend fun clearSection(sectionId: String): Unit = withContext(dispatcher) {
        queries.deleteBySection(sectionId)
    }
}
