package com.emm.gema.core.domain.fake

import com.emm.gema.core.domain.schoolyear.ActiveSchoolYearRepository
import com.emm.gema.core.domain.schoolyear.SchoolYearId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class InMemoryActiveSchoolYearRepository : ActiveSchoolYearRepository {

    private val activeId: MutableStateFlow<SchoolYearId?> = MutableStateFlow(null)

    override fun observeActiveId(): Flow<SchoolYearId?> = activeId

    override suspend fun activate(schoolYearId: SchoolYearId) {
        activeId.value = schoolYearId
    }
}
