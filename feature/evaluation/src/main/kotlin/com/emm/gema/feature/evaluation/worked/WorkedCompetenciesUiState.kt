package com.emm.gema.feature.evaluation.worked

import com.emm.gema.core.domain.curriculum.CompetencyId

data class WorkedCompetenciesUiState(
    val isLoading: Boolean = true,
    val areaName: String = "",
    val periodLabel: String = "",
    val competencies: List<CompetencyToggleRow> = emptyList(),
    val selectedCount: Int = 0,
)

data class CompetencyToggleRow(
    val id: CompetencyId,
    val siagieOrdinal: Int,
    val name: String,
    val isWorked: Boolean,
    val recordedLevelCount: Int,
)
