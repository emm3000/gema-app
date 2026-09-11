package com.emm.gema.feature.setup.section

import com.emm.gema.core.domain.section.Grade

data class SetupSectionUiState(
    val isLoading: Boolean = false,
    val grade: Grade? = null,
    val sectionName: String = "",
    val sectionNameError: String? = null,
    val canFinish: Boolean = false,
    val isSaving: Boolean = false,
)
