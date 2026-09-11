package com.emm.gema.core.database.schoolyear

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.emm.gema.core.database.ActiveSchoolYearQueries
import com.emm.gema.core.database.GemaDb
import com.emm.gema.core.domain.schoolyear.ActiveSchoolYearRepository
import com.emm.gema.core.domain.schoolyear.SchoolYearId
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SqlDelightActiveSchoolYearRepository(
    database: GemaDb,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ActiveSchoolYearRepository {

    private val queries: ActiveSchoolYearQueries = database.activeSchoolYearQueries

    override fun observeActiveId(): Flow<SchoolYearId?> = queries.select()
        .asFlow()
        .mapToOneOrNull(dispatcher)
        .map { it?.let(::SchoolYearId) }

    override suspend fun activate(schoolYearId: SchoolYearId): Unit = withContext(dispatcher) {
        queries.activate(schoolYearId.value)
    }
}
