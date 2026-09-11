package com.emm.gema.core.database.schoolyear

import com.emm.gema.core.database.School_year
import com.emm.gema.core.domain.schoolyear.PeriodKind
import com.emm.gema.core.domain.schoolyear.SchoolYear
import java.time.LocalDate

fun School_year.toDomain(): SchoolYear = SchoolYear(
    id = id,
    startDate = LocalDate.parse(start_date),
    endDate = LocalDate.parse(end_date),
    periodKind = PeriodKind.valueOf(period_kind),
)
