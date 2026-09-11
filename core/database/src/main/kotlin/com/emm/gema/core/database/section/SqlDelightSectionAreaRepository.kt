package com.emm.gema.core.database.section

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.emm.gema.core.database.GemaDb
import com.emm.gema.core.database.SectionHiddenAreaQueries
import com.emm.gema.core.domain.section.Area
import com.emm.gema.core.domain.section.SectionAreaRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SqlDelightSectionAreaRepository(
    database: GemaDb,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : SectionAreaRepository {

    private val queries: SectionHiddenAreaQueries = database.sectionHiddenAreaQueries

    override fun observeHiddenAreas(sectionId: String): Flow<Set<Area>> = queries.selectBySection(sectionId)
        .asFlow()
        .mapToList(dispatcher)
        .map { rows -> rows.mapNotNullTo(mutableSetOf()) { it.toAreaOrNull() } }

    override suspend fun setAreaHidden(sectionId: String, area: Area, isHidden: Boolean): Unit =
        withContext(dispatcher) {
            if (isHidden) {
                queries.insert(section_id = sectionId, area = area.name)
            } else {
                queries.delete(section_id = sectionId, area = area.name)
            }
        }

    override suspend fun clearSection(sectionId: String): Unit = withContext(dispatcher) {
        queries.deleteBySection(sectionId)
    }

    private fun String.toAreaOrNull(): Area? = Area.entries.find { it.name == this }
}
