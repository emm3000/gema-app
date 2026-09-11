package com.emm.gema.core.domain.fake

import com.emm.gema.core.domain.schoolyear.SchoolYear
import com.emm.gema.core.domain.schoolyear.SchoolYearRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class InMemorySchoolYearRepository : SchoolYearRepository {

    private val schoolYears: MutableStateFlow<List<SchoolYear>> = MutableStateFlow(emptyList())

    override fun observeAll(): Flow<List<SchoolYear>> = schoolYears
        .map { stored -> stored.sortedByDescending { it.startDate } }

    override suspend fun findById(id: String): SchoolYear? = schoolYears.value.find { it.id == id }

    override suspend fun save(schoolYear: SchoolYear) {
        schoolYears.value = schoolYears.value.filterNot { it.id == schoolYear.id } + schoolYear
    }
}
