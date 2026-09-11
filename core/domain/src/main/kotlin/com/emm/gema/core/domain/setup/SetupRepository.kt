package com.emm.gema.core.domain.setup

import com.emm.gema.core.domain.schoolyear.Period
import com.emm.gema.core.domain.schoolyear.SchoolYear
import com.emm.gema.core.domain.section.Section

interface SetupRepository {

    suspend fun saveAndActivate(schoolYear: SchoolYear, periods: List<Period>, section: Section)
}
