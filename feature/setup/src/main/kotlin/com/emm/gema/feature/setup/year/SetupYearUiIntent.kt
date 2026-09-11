package com.emm.gema.feature.setup.year

import com.emm.gema.core.domain.schoolyear.PeriodKind
import java.time.LocalDate

sealed interface SetupYearUiIntent {

    data class YearLabelChanged(val value: String) : SetupYearUiIntent

    data class StartDateChanged(val value: LocalDate) : SetupYearUiIntent

    data class EndDateChanged(val value: LocalDate) : SetupYearUiIntent

    data class PeriodKindSelected(val kind: PeriodKind) : SetupYearUiIntent

    data class PeriodStartDateChanged(val ordinal: Int, val value: LocalDate) : SetupYearUiIntent

    data class PeriodEndDateChanged(val ordinal: Int, val value: LocalDate) : SetupYearUiIntent

    data object ContinueClicked : SetupYearUiIntent

    data object BackClicked : SetupYearUiIntent
}
