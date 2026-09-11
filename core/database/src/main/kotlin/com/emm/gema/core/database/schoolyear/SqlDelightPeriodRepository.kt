package com.emm.gema.core.database.schoolyear

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.emm.gema.core.database.GemaDb
import com.emm.gema.core.database.PeriodQueries
import com.emm.gema.core.domain.schoolyear.Period
import com.emm.gema.core.domain.schoolyear.PeriodId
import com.emm.gema.core.domain.schoolyear.PeriodRepository
import com.emm.gema.core.domain.schoolyear.SchoolYearId
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SqlDelightPeriodRepository(
    private val database: GemaDb,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : PeriodRepository {

    private val queries: PeriodQueries = database.periodQueries

    override fun observeBySchoolYear(schoolYearId: SchoolYearId): Flow<List<Period>> =
        queries.selectBySchoolYear(schoolYearId.value)
            .asFlow()
            .mapToList(dispatcher)
            .map { rows -> rows.map { it.toDomain() } }

    override suspend fun findBySchoolYear(schoolYearId: SchoolYearId): List<Period> = withContext(dispatcher) {
        queries.selectBySchoolYear(schoolYearId.value).executeAsList().map { it.toDomain() }
    }

    override suspend fun findById(id: PeriodId): Period? = withContext(dispatcher) {
        queries.selectById(id.value).executeAsOneOrNull()?.toDomain()
    }

    override suspend fun saveAll(periods: List<Period>): Unit = withContext(dispatcher) {
        database.transaction {
            periods.forEach { period ->
                queries.insert(
                    id = period.id.value,
                    school_year_id = period.schoolYearId.value,
                    number = period.number.toLong(),
                    start_date = period.startDate.toString(),
                    end_date = period.endDate.toString(),
                )
            }
        }
    }
}
