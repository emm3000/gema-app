package com.emm.gema.core.database.section

import com.emm.gema.core.database.GemaDb
import com.emm.gema.core.domain.section.SectionCascade
import com.emm.gema.core.domain.section.SectionId
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SqlDelightSectionCascade(
    private val database: GemaDb,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : SectionCascade {

    override suspend fun deleteSection(sectionId: SectionId): Unit = withContext(dispatcher) {
        database.transaction {
            database.evidenceLevelQueries.deleteBySection(sectionId.value)
            database.activityQueries.deleteCompetenciesBySection(sectionId.value)
            database.activityQueries.deleteBySection(sectionId.value)
            database.attendanceQueries.deleteBySection(sectionId.value)
            database.periodLevelQueries.deleteBySection(sectionId.value)
            database.importedTemplateQueries.deleteBySection(sectionId.value)
            database.studentQueries.deleteBySection(sectionId.value)
            database.workedCompetencyQueries.deleteBySection(sectionId.value)
            database.sectionHiddenAreaQueries.deleteBySection(sectionId.value)
            database.sectionQueries.deleteById(sectionId.value)
        }
    }
}
