package com.emm.gema.feature.setup.year

import java.time.Clock
import java.time.LocalDate
import java.time.Month

private val firstSchoolMonth: Month = Month.MARCH
private const val FIRST_SCHOOL_DAY: Int = 1
private val lastSchoolMonth: Month = Month.DECEMBER
private const val LAST_SCHOOL_DAY: Int = 20

data class SchoolYearPrefill(
    val label: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
)

fun schoolYearPrefillFrom(clock: Clock): SchoolYearPrefill {
    val year: Int = LocalDate.now(clock).year
    return SchoolYearPrefill(
        label = year.toString(),
        startDate = LocalDate.of(year, firstSchoolMonth, FIRST_SCHOOL_DAY),
        endDate = LocalDate.of(year, lastSchoolMonth, LAST_SCHOOL_DAY),
    )
}
