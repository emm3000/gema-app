package com.emm.gema.core.database.section

import com.emm.gema.core.database.GemaDb
import com.emm.gema.core.domain.section.SectionCascade
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SqlDelightSectionCascade(
    private val database: GemaDb,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : SectionCascade {

    override suspend fun deleteSection(sectionId: String): Unit = withContext(dispatcher) {
        database.transaction {
            database.evidenceLevelQueries.deleteBySection(sectionId)
            database.activityQueries.deleteCompetenciesBySection(sectionId)
            database.activityQueries.deleteBySection(sectionId)
            database.attendanceQueries.deleteBySection(sectionId)
            database.periodLevelQueries.deleteBySection(sectionId)
            database.importedTemplateQueries.deleteBySection(sectionId)
            database.studentQueries.deleteBySection(sectionId)
            database.workedCompetencyQueries.deleteBySection(sectionId)
            database.sectionHiddenAreaQueries.deleteBySection(sectionId)
            database.sectionQueries.deleteById(sectionId)
        }
    }
}
