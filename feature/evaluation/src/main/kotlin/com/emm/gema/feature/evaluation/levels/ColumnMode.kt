package com.emm.gema.feature.evaluation.levels

fun PeriodLevelsUiState.enteringColumnMode(competencyId: String): PeriodLevelsUiState {
    if (columns.none { it.id == competencyId } || visibleRows.isEmpty()) return this

    return copy(
        sheet = null,
        columnMode = ColumnModeUiState(competencyId = competencyId, currentStudentIndex = 0),
    )
}

fun PeriodLevelsUiState.advancedToNextStudent(): PeriodLevelsUiState {
    val mode: ColumnModeUiState = columnMode ?: return this
    val nextIndex: Int = mode.currentStudentIndex + 1

    return copy(columnMode = mode.copy(currentStudentIndex = nextIndex).takeIf { nextIndex < visibleRows.size })
}

fun PeriodLevelsUiState.leavingColumnMode(): PeriodLevelsUiState = copy(columnMode = null)
