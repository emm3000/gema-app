package com.emm.gema.core.database.curriculum

import com.emm.gema.core.database.Competency as CompetencyRow
import com.emm.gema.core.domain.curriculum.Competency
import com.emm.gema.core.domain.section.Area

fun CompetencyRow.toDomain(): Competency = Competency(
    id = id,
    area = Area.valueOf(area),
    siagieOrdinal = siagie_ordinal.toInt(),
    name = name,
)
