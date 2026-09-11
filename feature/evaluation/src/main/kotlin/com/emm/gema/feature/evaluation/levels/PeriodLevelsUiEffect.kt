package com.emm.gema.feature.evaluation.levels

import com.emm.gema.core.domain.section.Area

sealed interface PeriodLevelsUiEffect {

    data class NavigateToWorkedCompetencies(
        val sectionId: String,
        val periodId: String,
        val area: Area,
    ) : PeriodLevelsUiEffect

    data object NavigateBack : PeriodLevelsUiEffect

    data class ShowMessage(val text: String) : PeriodLevelsUiEffect
}
