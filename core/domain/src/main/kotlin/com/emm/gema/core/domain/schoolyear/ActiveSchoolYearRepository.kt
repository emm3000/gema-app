package com.emm.gema.core.domain.schoolyear

import kotlinx.coroutines.flow.Flow

interface ActiveSchoolYearRepository {

    fun observeActiveId(): Flow<SchoolYearId?>

    suspend fun activate(schoolYearId: SchoolYearId)
}
