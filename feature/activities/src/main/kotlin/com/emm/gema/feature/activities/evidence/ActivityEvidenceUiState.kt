package com.emm.gema.feature.activities.evidence

import com.emm.gema.core.domain.curriculum.CompetencyId
import com.emm.gema.core.domain.evaluation.AchievementLevel
import com.emm.gema.core.domain.section.SectionId
import com.emm.gema.core.domain.student.StudentId

data class ActivityEvidenceUiState(
    val isLoading: Boolean = true,
    val sectionId: SectionId? = null,
    val activityName: String = "",
    val activityDateLabel: String = "",
    val periodLabel: String = "",
    val competencies: List<CompetencyColumn> = emptyList(),
    val selectedCompetencyId: CompetencyId? = null,
    val recordedCount: Int = 0,
    val totalCount: Int = 0,
    val rows: List<EvidenceLevelRow> = emptyList(),
)

data class CompetencyColumn(
    val id: CompetencyId,
    val label: String,
)

data class EvidenceLevelRow(
    val studentId: StudentId,
    val displayName: String,
    val level: AchievementLevel?,
)
