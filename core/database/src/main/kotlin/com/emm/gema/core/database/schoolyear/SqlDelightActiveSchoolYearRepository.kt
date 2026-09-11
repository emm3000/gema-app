package com.emm.gema.core.database.schoolyear

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.emm.gema.core.database.ActiveSchoolYearQueries
import com.emm.gema.core.database.GemaDb
import com.emm.gema.core.domain.schoolyear.ActiveSchoolYearRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class SqlDelightActiveSchoolYearRepository(
    database: GemaDb,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ActiveSchoolYearRepository {

    private val queries: ActiveSchoolYearQueries = database.activeSchoolYearQueries

    override fun observeActiveId(): Flow<String?> = queries.select()
        .asFlow()
        .mapToOneOrNull(dispatcher)

    override suspend fun activate(schoolYearId: String): Unit = withContext(dispatcher) {
        queries.activate(schoolYearId)
    }
}
