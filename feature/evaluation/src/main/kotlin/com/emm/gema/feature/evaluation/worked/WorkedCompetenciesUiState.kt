package com.emm.gema.feature.evaluation.worked

data class WorkedCompetenciesUiState(
    val isLoading: Boolean = true,
    val areaName: String = "",
    val periodLabel: String = "",
    val competencies: List<CompetencyToggleRow> = emptyList(),
    val selectedCount: Int = 0,
)

data class CompetencyToggleRow(
    val id: String,
    val siagieOrdinal: Int,
    val name: String,
    val isWorked: Boolean,
    val recordedLevelCount: Int,
)
