package com.emm.gema.feature.evaluation.worked

import com.emm.gema.core.domain.curriculum.CompetencyId

sealed interface WorkedCompetenciesUiIntent {

    data class CompetencyToggled(val id: CompetencyId, val isWorked: Boolean) : WorkedCompetenciesUiIntent

    data object BackClicked : WorkedCompetenciesUiIntent
}
