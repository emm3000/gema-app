package com.emm.gema.feature.setup

import com.emm.gema.core.domain.schoolyear.PeriodDates
import java.time.LocalDate

internal const val OUTSIDE_YEAR_ERROR: String = "Este periodo se sale del año escolar"
internal const val OVERLAP_ERROR: String = "Este periodo se superpone con otro"
internal const val INVERTED_PERIOD_ERROR: String = "Este periodo termina antes de empezar"

internal fun PeriodDates.errorWithin(
    periods: List<PeriodDates>,
    yearStart: LocalDate,
    yearEnd: LocalDate,
): String? {
    if (endDate.isBefore(startDate)) return INVERTED_PERIOD_ERROR
    if (startDate.isBefore(yearStart) || endDate.isAfter(yearEnd)) return OUTSIDE_YEAR_ERROR

    val overlaps: Boolean = periods
        .filterNot { it.number == number }
        .any { other -> other.startDate <= endDate && startDate <= other.endDate }
    return OVERLAP_ERROR.takeIf { overlaps }
}
