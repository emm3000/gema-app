package com.emm.gema.feature.export

sealed interface ExportUiIntent {

    data object ExportGradesClicked : ExportUiIntent

    data class GapRowClicked(val row: ExportGapRow) : ExportUiIntent

    data object ImportTemplateClicked : ExportUiIntent

    data object ExportAttendanceClicked : ExportUiIntent

    data class AttendanceTemplatePicked(val uri: String) : ExportUiIntent

    data object ExportSummaryCsvClicked : ExportUiIntent

    data object ExportSummaryPdfClicked : ExportUiIntent

    data object BackClicked : ExportUiIntent
}
