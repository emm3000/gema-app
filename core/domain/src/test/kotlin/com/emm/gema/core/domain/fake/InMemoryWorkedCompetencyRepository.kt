package com.emm.gema.core.domain.fake

import com.emm.gema.core.domain.curriculum.WorkedCompetencyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class InMemoryWorkedCompetencyRepository : WorkedCompetencyRepository {

    val rows: MutableStateFlow<Set<Triple<String, String, String>>> = MutableStateFlow(emptySet())

    override fun observeWorked(sectionId: String, periodId: String): Flow<Set<String>> = rows.map { current ->
        current.filter { it.first == sectionId && it.second == periodId }.mapTo(mutableSetOf()) { it.third }
    }

    override suspend fun setWorked(
        sectionId: String,
        periodId: String,
        competencyId: String,
        isWorked: Boolean,
    ) {
        val row: Triple<String, String, String> = Triple(sectionId, periodId, competencyId)
        rows.value = if (isWorked) rows.value + row else rows.value - row
    }

    override suspend fun clearSection(sectionId: String) {
        rows.value = rows.value.filterNotTo(mutableSetOf()) { it.first == sectionId }
    }
}
