package com.emm.gema.core.domain.activity

import com.emm.gema.core.domain.curriculum.CompetencyId
import com.emm.gema.core.domain.schoolyear.PeriodId
import com.emm.gema.core.domain.section.SectionId
import java.time.LocalDate

data class Activity(
    val id: ActivityId,
    val sectionId: SectionId,
    val periodId: PeriodId,
    val name: String,
    val date: LocalDate,
    val competencyIds: Set<CompetencyId>,
) {
    init {
        require(name.isNotBlank()) { "An activity needs a name" }
        require(competencyIds.isNotEmpty()) { "An activity needs at least one competency" }
    }
}
