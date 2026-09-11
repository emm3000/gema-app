package com.emm.gema.core.domain.section

import com.emm.gema.core.domain.schoolyear.SchoolYearId
import kotlinx.coroutines.flow.Flow

class GetSectionCountsUseCase(
    private val repository: SectionRepository,
) {

    operator fun invoke(): Flow<Map<SchoolYearId, Int>> = repository.observeCountsBySchoolYear()
}
