package com.emm.gema.core.database.curriculum

import com.emm.gema.core.database.CompetencyQueries
import com.emm.gema.core.database.GemaDb
import com.emm.gema.core.domain.curriculum.Competency
import com.emm.gema.core.domain.curriculum.CompetencyRepository
import com.emm.gema.core.domain.section.Area
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SqlDelightCompetencyRepository(
    database: GemaDb,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : CompetencyRepository {

    private val queries: CompetencyQueries = database.competencyQueries

    override suspend fun seed(competencies: List<Competency>, curriculumVersion: Int): Unit =
        withContext(dispatcher) {
            queries.transaction {
                competencies.forEach { competency ->
                    queries.upsert(
                        id = competency.id,
                        area = competency.area.name,
                        siagie_ordinal = competency.siagieOrdinal.toLong(),
                        name = competency.name,
                        curriculum_version = curriculumVersion.toLong(),
                    )
                }
            }
        }

    override suspend fun findByArea(area: Area): List<Competency> = withContext(dispatcher) {
        queries.selectByArea(area.name).executeAsList().map { it.toDomain() }
    }
}
