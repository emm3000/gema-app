package com.emm.gema.core.domain.schoolyear

import java.time.LocalDate

data class Period(
    val id: PeriodId,
    val schoolYearId: SchoolYearId,
    val number: Int,
    val startDate: LocalDate,
    val endDate: LocalDate,
) {
    init {
        require(number >= FIRST_PERIOD_NUMBER) { "A period is numbered from $FIRST_PERIOD_NUMBER" }
        require(!endDate.isBefore(startDate)) { "A period ends on or after it starts" }
    }

    fun contains(date: LocalDate): Boolean = !date.isBefore(startDate) && !date.isAfter(endDate)

    companion object {
        const val FIRST_PERIOD_NUMBER: Int = 1
    }
}
