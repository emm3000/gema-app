package com.emm.gema.feature.sections.form

import com.emm.gema.core.domain.section.Grade

data class SectionFormUiState(
    val isLoading: Boolean = true,
    val sectionId: String? = null,
    val grade: Grade? = null,
    val sectionName: String = "",
    val sectionNameError: String? = null,
    val canSave: Boolean = false,
    val canDelete: Boolean = false,
    val deleteConfirmation: DeleteConfirmation? = null,
)

data class DeleteConfirmation(
    val studentCount: Int,
    val attendanceDayCount: Int,
    val periodLevelCount: Int,
)
