package com.emm.gema.feature.setup.year

sealed interface SetupYearUiEffect {

    data class NavigateToSetupSection(val draft: SchoolYearDraft) : SetupYearUiEffect
}
