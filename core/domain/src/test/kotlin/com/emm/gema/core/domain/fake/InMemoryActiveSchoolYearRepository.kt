package com.emm.gema.core.domain.fake

import com.emm.gema.core.domain.schoolyear.ActiveSchoolYearRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class InMemoryActiveSchoolYearRepository : ActiveSchoolYearRepository {

    private val activeId: MutableStateFlow<String?> = MutableStateFlow(null)

    override fun observeActiveId(): Flow<String?> = activeId

    override suspend fun activate(schoolYearId: String) {
        activeId.value = schoolYearId
    }
}
