package com.emm.gema.feature.activities.evidence

import com.emm.gema.core.domain.evaluation.AchievementLevel

data class ActivityEvidenceUiState(
    val isLoading: Boolean = true,
    val sectionId: String = "",
    val activityName: String = "",
    val activityDateLabel: String = "",
    val periodLabel: String = "",
    val competencies: List<CompetencyColumn> = emptyList(),
    val selectedCompetencyId: String? = null,
    val recordedCount: Int = 0,
    val totalCount: Int = 0,
    val rows: List<EvidenceLevelRow> = emptyList(),
)

data class CompetencyColumn(
    val id: String,
    val label: String,
)

data class EvidenceLevelRow(
    val studentId: String,
    val displayName: String,
    val level: AchievementLevel?,
)
