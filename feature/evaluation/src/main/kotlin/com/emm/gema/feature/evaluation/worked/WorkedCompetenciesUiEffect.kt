package com.emm.gema.feature.evaluation.worked

sealed interface WorkedCompetenciesUiEffect {

    data object NavigateBack : WorkedCompetenciesUiEffect

    data class ShowMessage(val text: String) : WorkedCompetenciesUiEffect
}
