package com.emm.gema.core.domain.curriculum

import com.emm.gema.core.domain.section.Area

interface CompetencyRepository {

    suspend fun seed(competencies: List<Competency>, curriculumVersion: Int)

    suspend fun findByArea(area: Area): List<Competency>
}
