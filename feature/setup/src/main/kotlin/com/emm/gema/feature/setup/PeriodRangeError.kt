package com.emm.gema.feature.setup

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.emm.gema.core.domain.schoolyear.PeriodDates
import java.time.LocalDate

enum class PeriodRangeError {
    INVERTED,
    OUTSIDE_YEAR,
    OVERLAP,
}

@Composable
fun PeriodRangeError.resolve(): String = when (this) {
    PeriodRangeError.INVERTED -> stringResource(R.string.setup_period_range_error_inverted)
    PeriodRangeError.OUTSIDE_YEAR -> stringResource(R.string.setup_period_range_error_outside_year)
    PeriodRangeError.OVERLAP -> stringResource(R.string.setup_period_range_error_overlap)
}

fun PeriodDates.errorWithin(
    periods: List<PeriodDates>,
    yearStart: LocalDate,
    yearEnd: LocalDate,
): PeriodRangeError? {
    if (endDate.isBefore(startDate)) return PeriodRangeError.INVERTED
    if (startDate.isBefore(yearStart) || endDate.isAfter(yearEnd)) return PeriodRangeError.OUTSIDE_YEAR

    val hasOverlap: Boolean = periods
        .filterNot { it.number == number }
        .any { other -> other.startDate <= endDate && startDate <= other.endDate }
    return PeriodRangeError.OVERLAP.takeIf { hasOverlap }
}
