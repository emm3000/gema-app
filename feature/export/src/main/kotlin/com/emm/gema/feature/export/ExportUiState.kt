package com.emm.gema.feature.export

import com.emm.gema.core.domain.curriculum.CompetencyId
import com.emm.gema.core.domain.schoolyear.PeriodId
import com.emm.gema.core.domain.student.StudentId
import java.time.YearMonth

data class ExportUiState(
    val isLoading: Boolean = true,
    val sectionTitle: String = "",
    val periodId: PeriodId? = null,
    val periodLabel: String = "",
    val templateFileName: String? = null,
    val gradesExportState: GradesExportUiState = GradesExportUiState.Unavailable,
    val templateMismatch: TemplateMismatchUi? = null,
    val attendanceMonth: YearMonth? = null,
    val attendanceDayCount: Int = 0,
    val activeExport: ActiveExport? = null,
)

enum class ActiveExport {
    GRADES,
    ATTENDANCE,
    SUMMARY_CSV,
    SUMMARY_PDF,
}

data class TemplateMismatchUi(
    val areaNames: List<String>,
    val studentNames: List<String>,
    val competencyLabels: List<String>,
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
