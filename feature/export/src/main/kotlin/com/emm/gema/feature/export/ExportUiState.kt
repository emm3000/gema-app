package com.emm.gema.feature.export

data class ExportUiState(
    val isLoading: Boolean = true,
    val sectionTitle: String = "",
    val periods: List<PeriodOption> = emptyList(),
    val selectedPeriodId: String? = null,
    val templateFileName: String? = null,
    val gradesExportState: GradesExportUiState = GradesExportUiState.Unavailable,
    val templateMismatch: TemplateMismatchUi? = null,
    val isExporting: Boolean = false,
)

data class TemplateMismatchUi(
    val areaNames: List<String>,
    val studentNames: List<String>,
)

data class PeriodOption(
    val id: String,
    val label: String,
    val isCurrent: Boolean,
)

sealed interface GradesExportUiState {

    data object Unavailable : GradesExportUiState

    data object Ready : GradesExportUiState

    data class Blocked(val gaps: List<ExportGapRow>) : GradesExportUiState
}

data class ExportGapRow(
    val studentId: String,
    val studentName: String,
    val competencyId: String,
    val competencyLabel: String,
)
