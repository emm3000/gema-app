package com.emm.gema.core.domain.schoolyear

import java.time.LocalDate

data class Period(
    val id: String,
    val schoolYearId: String,
    val number: Int,
    val startDate: LocalDate,
    val endDate: LocalDate,
) {
    init {
        require(id.isNotBlank()) { "A period needs an id" }
        require(schoolYearId.isNotBlank()) { "A period belongs to a school year" }
        require(number >= FIRST_PERIOD_NUMBER) { "A period is numbered from $FIRST_PERIOD_NUMBER" }
        require(!endDate.isBefore(startDate)) { "A period ends on or after it starts" }
    }

    fun contains(date: LocalDate): Boolean = !date.isBefore(startDate) && !date.isAfter(endDate)

    companion object {
        const val FIRST_PERIOD_NUMBER: Int = 1
    }
}
