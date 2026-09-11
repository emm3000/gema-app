package com.emm.gema.feature.evaluation.worked

sealed interface WorkedCompetenciesUiIntent {

    data class CompetencyToggled(val id: String, val isWorked: Boolean) : WorkedCompetenciesUiIntent

    data object BackClicked : WorkedCompetenciesUiIntent
}
