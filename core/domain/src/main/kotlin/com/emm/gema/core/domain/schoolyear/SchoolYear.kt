package com.emm.gema.core.domain.schoolyear

import java.time.LocalDate

data class SchoolYear(
    val id: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val periodKind: PeriodKind,
) {
    init {
        require(id.isNotBlank()) { "A school year needs an id" }
        require(endDate.isAfter(startDate)) { "A school year ends after it starts" }
    }
}
