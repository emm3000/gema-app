package com.emm.gema.feature.backup

import com.emm.gema.core.domain.schoolyear.SchoolYear
import com.emm.gema.core.domain.schoolyear.SchoolYearRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeSchoolYearRepository(
    vararg schoolYears: SchoolYear,
) : SchoolYearRepository {

    private val stored: MutableStateFlow<List<SchoolYear>> = MutableStateFlow(schoolYears.toList())

    override fun observeAll(): Flow<List<SchoolYear>> = stored

    override suspend fun findById(id: String): SchoolYear? = stored.value.find { it.id == id }

    override suspend fun save(schoolYear: SchoolYear) {
        stored.value = stored.value + schoolYear
    }
}
