package com.emm.gema.core.domain.fake

import com.emm.gema.core.domain.curriculum.Competency
import com.emm.gema.core.domain.curriculum.CompetencyRepository
import com.emm.gema.core.domain.section.Area

class InMemoryCompetencyRepository : CompetencyRepository {

    private val rows: MutableMap<String, Competency> = linkedMapOf()

    var seededVersion: Int? = null
        private set

    var seedCallCount: Int = 0
        private set

    override suspend fun seed(competencies: List<Competency>, curriculumVersion: Int) {
        seedCallCount += 1
        seededVersion = curriculumVersion
        competencies.forEach { rows[it.id] = it }
    }

    override suspend fun findByArea(area: Area): List<Competency> = rows.values
        .filter { it.area == area }
        .sortedBy { it.siagieOrdinal }

    fun all(): List<Competency> = rows.values.toList()
}
