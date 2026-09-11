package com.emm.gema.core.database.activity

import com.emm.gema.core.database.SelectByPeriod
import com.emm.gema.core.database.SelectById
import com.emm.gema.core.domain.activity.Activity
import java.time.LocalDate

fun SelectByPeriod.toDomain(): Activity = Activity(
    id = id,
    sectionId = section_id,
    periodId = period_id,
    name = name,
    date = LocalDate.parse(date),
    competencyIds = competency_ids.toCompetencyIds(),
)

fun SelectById.toDomain(): Activity = Activity(
    id = id,
    sectionId = section_id,
    periodId = period_id,
    name = name,
    date = LocalDate.parse(date),
    competencyIds = competency_ids.toCompetencyIds(),
)

private fun String?.toCompetencyIds(): Set<String> = this?.split(",")?.filter { it.isNotBlank() }?.toSet().orEmpty()
