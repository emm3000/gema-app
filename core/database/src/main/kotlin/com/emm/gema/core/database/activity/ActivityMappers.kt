package com.emm.gema.core.database.activity

import com.emm.gema.core.database.SelectById
import com.emm.gema.core.database.SelectByPeriod
import com.emm.gema.core.domain.activity.Activity
import com.emm.gema.core.domain.activity.ActivityId
import com.emm.gema.core.domain.curriculum.CompetencyId
import com.emm.gema.core.domain.schoolyear.PeriodId
import com.emm.gema.core.domain.section.SectionId
import java.time.LocalDate

fun SelectByPeriod.toDomain(): Activity = Activity(
    id = ActivityId(id),
    sectionId = SectionId(section_id),
    periodId = PeriodId(period_id),
    name = name,
    date = LocalDate.parse(date),
    competencyIds = competency_ids.toCompetencyIds(),
)

fun SelectById.toDomain(): Activity = Activity(
    id = ActivityId(id),
    sectionId = SectionId(section_id),
    periodId = PeriodId(period_id),
    name = name,
    date = LocalDate.parse(date),
    competencyIds = competency_ids.toCompetencyIds(),
)

private fun String?.toCompetencyIds(): Set<CompetencyId> = this?.split(",")
    ?.filter { it.isNotBlank() }
    ?.mapTo(mutableSetOf(), ::CompetencyId)
    .orEmpty()
