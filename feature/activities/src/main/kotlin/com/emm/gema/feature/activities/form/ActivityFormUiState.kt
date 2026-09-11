package com.emm.gema.feature.activities.form

import com.emm.gema.core.domain.activity.ActivityId
import com.emm.gema.core.domain.curriculum.CompetencyId
import com.emm.gema.core.domain.section.Grade
import java.time.LocalDate

data class ActivityFormUiState(
    val isLoading: Boolean = true,
    val activityId: ActivityId? = null,
    val grade: Grade? = null,
    val sectionName: String = "",
    val name: String = "",
    val date: LocalDate? = null,
    val dateError: ActivityFormMessage? = null,
    val resolvedPeriodLabel: String? = null,
    val hasPeriodChangeWarning: Boolean = false,
    val competencyGroups: List<CompetencyGroup> = emptyList(),
    val selectedCompetencyIds: Set<CompetencyId> = emptySet(),
    val isDeleteConfirmVisible: Boolean = false,
) {
    val canSave: Boolean
        get() = name.isNotBlank() && date != null && resolvedPeriodLabel != null && selectedCompetencyIds.isNotEmpty()

    val canDelete: Boolean
        get() = activityId != null
}

data class CompetencyGroup(
    val areaName: String,
    val competencies: List<CompetencyToggleRow>,
)

data class CompetencyToggleRow(
    val id: CompetencyId,
    val siagieOrdinal: Int,
    val name: String,
)
