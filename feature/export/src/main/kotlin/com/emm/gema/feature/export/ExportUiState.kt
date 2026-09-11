package com.emm.gema.feature.export

import com.emm.gema.core.domain.curriculum.CompetencyId
import com.emm.gema.core.domain.schoolyear.PeriodId
import com.emm.gema.core.domain.student.StudentId

data class ExportUiState(
    val isLoading: Boolean = true,
    val sectionTitle: String = "",
    val periods: List<PeriodOption> = emptyList(),
    val selectedPeriodId: PeriodId? = null,
    val templateFileName: String? = null,
    val gradesExportState: GradesExportUiState = GradesExportUiState.Unavailable,
    val templateMismatch: TemplateMismatchUi? = null,
    val activeExport: ActiveExport? = null,
)

enum class ActiveExport {
    GRADES,
    SUMMARY_CSV,
    SUMMARY_PDF,
}

data class TemplateMismatchUi(
    val areaNames: List<String>,
    val studentNames: List<String>,
    val competencyLabels: List<String>,
)

data class PeriodOption(
    val id: PeriodId,
    val label: String,
    val isCurrent: Boolean,
)

sealed interface GradesExportUiState {

    data object Unavailable : GradesExportUiState

    data object Ready : GradesExportUiState

    data class Blocked(val gaps: List<ExportGapRow>) : GradesExportUiState
}

data class ExportGapRow(
    val studentId: StudentId,
    val studentName: String,
    val competencyId: CompetencyId,
    val competencyLabel: String,
)
