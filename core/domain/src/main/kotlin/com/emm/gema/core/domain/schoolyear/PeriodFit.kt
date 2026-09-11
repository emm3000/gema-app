package com.emm.gema.core.domain.schoolyear

fun requirePeriodsFit(schoolYear: SchoolYear, periods: List<Period>) {
    periods.forEach { period ->
        require(period.schoolYearId == schoolYear.id) {
            "Period ${period.number} belongs to another school year"
        }
        require(!period.startDate.isBefore(schoolYear.startDate) && !period.endDate.isAfter(schoolYear.endDate)) {
            "Period ${period.number} stays inside its school year"
        }
    }
    periods.sortedBy { it.startDate }.zipWithNext { earlier, later ->
        require(earlier.endDate.isBefore(later.startDate)) {
            "Periods ${earlier.number} and ${later.number} overlap"
        }
    }
}
