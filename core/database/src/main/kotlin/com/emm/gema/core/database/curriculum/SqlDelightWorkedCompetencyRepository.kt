package com.emm.gema.core.database.curriculum

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.emm.gema.core.database.GemaDb
import com.emm.gema.core.database.WorkedCompetencyQueries
import com.emm.gema.core.domain.curriculum.CompetencyId
import com.emm.gema.core.domain.curriculum.WorkedCompetencyRepository
import com.emm.gema.core.domain.schoolyear.PeriodId
import com.emm.gema.core.domain.section.SectionId
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

    override fun observeWorked(sectionId: SectionId, periodId: PeriodId): Flow<Set<CompetencyId>> = queries
        .selectBySectionAndPeriod(section_id = sectionId.value, period_id = periodId.value)
        .asFlow()
        .mapToList(dispatcher)
        .map { rows -> rows.mapTo(mutableSetOf(), ::CompetencyId) }

    override suspend fun setWorked(
        sectionId: SectionId,
        periodId: PeriodId,
        competencyId: CompetencyId,
        isWorked: Boolean,
    ): Unit = withContext(dispatcher) {
        if (isWorked) {
            queries.insert(section_id = sectionId.value, period_id = periodId.value, competency_id = competencyId.value)
        } else {
            queries.delete(section_id = sectionId.value, period_id = periodId.value, competency_id = competencyId.value)
        }
    }

    override suspend fun clearSection(sectionId: SectionId): Unit = withContext(dispatcher) {
        queries.deleteBySection(sectionId.value)
    }
}
