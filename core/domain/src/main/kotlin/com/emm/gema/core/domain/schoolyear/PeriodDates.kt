package com.emm.gema.core.domain.schoolyear

import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class PeriodDates(
    val number: Int,
    val startDate: LocalDate,
    val endDate: LocalDate,
)

fun PeriodKind.divide(startDate: LocalDate, endDate: LocalDate): List<PeriodDates> {
    val totalDays: Long = ChronoUnit.DAYS.between(startDate, endDate) + 1
    require(totalDays >= periodCount) {
        "A $name school year needs at least $periodCount days"
    }
    val shortestLength: Long = totalDays / periodCount
    val periodsWithAnExtraDay: Long = totalDays % periodCount

    var periodStart: LocalDate = startDate
    return (0 until periodCount).map { index ->
        val length: Long = shortestLength + if (index < periodsWithAnExtraDay) 1 else 0
        val periodEnd: LocalDate = periodStart.plusDays(length - 1)
        val dates = PeriodDates(
            number = index + Period.FIRST_PERIOD_NUMBER,
            startDate = periodStart,
            endDate = periodEnd,
        )
        periodStart = periodEnd.plusDays(1)
        dates
    }
}
