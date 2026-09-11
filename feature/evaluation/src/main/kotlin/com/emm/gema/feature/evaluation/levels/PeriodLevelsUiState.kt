package com.emm.gema.feature.evaluation.levels

import com.emm.gema.core.domain.evaluation.AchievementLevel
import com.emm.gema.core.domain.evaluation.UnworkedComment
import com.emm.gema.core.domain.section.Area

data class PeriodLevelsUiState(
    val isLoading: Boolean = true,
    val sectionTitle: String = "",
    val areas: List<AreaOption> = emptyList(),
    val selectedArea: Area? = null,
    val periods: List<PeriodOption> = emptyList(),
    val selectedPeriodId: String? = null,
    val columns: List<CompetencyColumn> = emptyList(),
    val rows: List<PeriodLevelRow> = emptyList(),
    val missingCount: Int = 0,
    val isMissingFilterOn: Boolean = false,
    val hasWorkedCompetencies: Boolean = false,
    val sheet: PeriodLevelSheetUiState? = null,
    val columnMode: ColumnModeUiState? = null,
) {
    val visibleRows: List<PeriodLevelRow>
        get() = if (isMissingFilterOn) rows.filter { row -> row.cells.any { !it.isRecorded } } else rows

    val periodLabel: String
        get() = periods.find { it.id == selectedPeriodId }?.label.orEmpty()

    val columnModeStudent: PeriodLevelRow?
        get() = columnMode?.let { visibleRows.getOrNull(it.currentStudentIndex) }

    val columnModeColumn: CompetencyColumn?
        get() = columnMode?.let { mode -> columns.find { it.id == mode.competencyId } }
}

data class AreaOption(
    val area: Area,
    val name: String,
)

data class PeriodOption(
    val id: String,
    val label: String,
    val isCurrent: Boolean,
)

data class CompetencyColumn(
    val id: String,
    val siagieOrdinal: Int,
    val name: String,
)

data class PeriodLevelRow(
    val studentId: String,
    val displayName: String,
    val cells: List<PeriodLevelCell>,
)

data class PeriodLevelCellKey(
    val studentId: String,
    val competencyId: String,
)

data class PeriodLevelCell(
    val competencyId: String,
    val achievementLevel: AchievementLevel?,
    val unworkedComment: UnworkedComment?,
    val hasDescriptiveConclusion: Boolean,
    val isIncomplete: Boolean,
) {
    val isRecorded: Boolean get() = achievementLevel != null || unworkedComment != null
}

data class PeriodLevelSheetUiState(
    val studentId: String,
    val competencyId: String,
    val studentName: String,
    val competencyLabel: String,
    val achievementLevel: AchievementLevel? = null,
    val unworkedComment: UnworkedComment? = null,
    val descriptiveConclusion: String = "",
    val isConclusionRequiredForExport: Boolean = false,
)

data class ColumnModeUiState(
    val competencyId: String,
    val currentStudentIndex: Int,
)
