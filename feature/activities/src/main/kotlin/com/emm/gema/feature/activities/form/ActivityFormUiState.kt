package com.emm.gema.feature.activities.form

import java.time.LocalDate

data class ActivityFormUiState(
    val isLoading: Boolean = true,
    val activityId: String? = null,
    val name: String = "",
    val date: LocalDate? = null,
    val dateError: String? = null,
    val resolvedPeriodLabel: String? = null,
    val periodChangeWarning: String? = null,
    val competencyGroups: List<CompetencyGroup> = emptyList(),
    val selectedCompetencyIds: Set<String> = emptySet(),
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
    val id: String,
    val siagieOrdinal: Int,
    val name: String,
)
