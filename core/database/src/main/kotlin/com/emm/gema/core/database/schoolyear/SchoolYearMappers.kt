package com.emm.gema.core.database.schoolyear

import com.emm.gema.core.database.Period as PeriodRow
import com.emm.gema.core.database.School_year
import com.emm.gema.core.domain.schoolyear.Period
import com.emm.gema.core.domain.schoolyear.PeriodKind
import com.emm.gema.core.domain.schoolyear.SchoolYear
import java.time.LocalDate

fun School_year.toDomain(): SchoolYear = SchoolYear(
    id = id,
    label = label,
    startDate = LocalDate.parse(start_date),
    endDate = LocalDate.parse(end_date),
    periodKind = PeriodKind.valueOf(period_kind),
)

fun PeriodRow.toDomain(): Period = Period(
    id = id,
    schoolYearId = school_year_id,
    number = number.toInt(),
    startDate = LocalDate.parse(start_date),
    endDate = LocalDate.parse(end_date),
)
