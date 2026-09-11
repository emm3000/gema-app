package com.emm.gema.feature.setup.year

import com.emm.gema.core.domain.schoolyear.PeriodKind
import java.time.LocalDate

sealed interface SetupYearUiIntent {

    data class YearLabelChanged(val value: String) : SetupYearUiIntent

    data class StartDateChanged(val value: LocalDate) : SetupYearUiIntent

    data class EndDateChanged(val value: LocalDate) : SetupYearUiIntent

    data class PeriodKindSelected(val kind: PeriodKind) : SetupYearUiIntent

    data class PeriodClicked(val ordinal: Int) : SetupYearUiIntent

    data class EditorStartDateChanged(val value: LocalDate) : SetupYearUiIntent

    data class EditorEndDateChanged(val value: LocalDate) : SetupYearUiIntent

    data object EditorConfirmed : SetupYearUiIntent

    data object EditorDismissed : SetupYearUiIntent

    data object ContinueClicked : SetupYearUiIntent

    data object BackClicked : SetupYearUiIntent
}
