package com.emm.gema.core.domain.setup

import com.emm.gema.core.domain.schoolyear.SchoolYear
import com.emm.gema.core.domain.section.Section

data class CompletedSetup(
    val schoolYear: SchoolYear,
    val section: Section,
)
