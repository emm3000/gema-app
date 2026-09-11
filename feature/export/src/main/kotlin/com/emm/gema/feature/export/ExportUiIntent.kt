package com.emm.gema.feature.export

import com.emm.gema.core.domain.schoolyear.PeriodId

sealed interface ExportUiIntent {

    data class PeriodSelected(val periodId: PeriodId) : ExportUiIntent

    data object ExportGradesClicked : ExportUiIntent

    data class GapRowClicked(val row: ExportGapRow) : ExportUiIntent

    data object ImportTemplateClicked : ExportUiIntent

    data object ExportSummaryCsvClicked : ExportUiIntent

    data object ExportSummaryPdfClicked : ExportUiIntent

    data object BackClicked : ExportUiIntent
}
