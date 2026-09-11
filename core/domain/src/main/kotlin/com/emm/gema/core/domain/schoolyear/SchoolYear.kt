package com.emm.gema.core.domain.schoolyear

import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class SchoolYear(
    val id: String,
    val label: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val periodKind: PeriodKind,
) {
    init {
        require(id.isNotBlank()) { "A school year needs an id" }
        require(label.isNotBlank()) { "A school year needs a label" }
        require(endDate.isAfter(startDate)) { "A school year ends after it starts" }
        require(ChronoUnit.DAYS.between(startDate, endDate) + 1 >= periodKind.periodCount) {
            "A school year needs at least one day per period"
        }
    }
}
