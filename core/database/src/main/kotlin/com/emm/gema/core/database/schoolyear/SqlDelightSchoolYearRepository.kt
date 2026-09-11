package com.emm.gema.core.database.schoolyear

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.emm.gema.core.database.GemaDb
import com.emm.gema.core.database.SchoolYearQueries
import com.emm.gema.core.domain.schoolyear.SchoolYear
import com.emm.gema.core.domain.schoolyear.SchoolYearRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SqlDelightSchoolYearRepository(
    database: GemaDb,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : SchoolYearRepository {

    private val queries: SchoolYearQueries = database.schoolYearQueries

    override fun observeAll(): Flow<List<SchoolYear>> = queries.selectAll()
        .asFlow()
        .mapToList(dispatcher)
        .map { rows -> rows.map { it.toDomain() } }

    override suspend fun findById(id: String): SchoolYear? = queries.selectById(id)
        .asFlow()
        .mapToOneOrNull(dispatcher)
        .map { row -> row?.toDomain() }
        .first()

    override suspend fun save(schoolYear: SchoolYear): Unit = withContext(dispatcher) {
        queries.insert(
            id = schoolYear.id,
            start_date = schoolYear.startDate.toString(),
            end_date = schoolYear.endDate.toString(),
            period_kind = schoolYear.periodKind.name,
        )
    }
}
