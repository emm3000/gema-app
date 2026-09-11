package com.emm.gema.core.domain.curriculum

import kotlinx.coroutines.flow.Flow

interface WorkedCompetencyRepository {

    fun observeWorked(sectionId: String, periodId: String): Flow<Set<String>>

    suspend fun setWorked(sectionId: String, periodId: String, competencyId: String, isWorked: Boolean)

    suspend fun clearSection(sectionId: String)
}
