package com.emm.gema.feature.activities.form

import com.emm.gema.core.domain.curriculum.CompetencyId
import java.time.LocalDate

sealed interface ActivityFormUiIntent {

    data class NameChanged(val value: String) : ActivityFormUiIntent

    data class DateChanged(val value: LocalDate) : ActivityFormUiIntent

    data class CompetencyToggled(val id: CompetencyId, val isSelected: Boolean) : ActivityFormUiIntent

    data object SaveClicked : ActivityFormUiIntent

    data object DeleteClicked : ActivityFormUiIntent

    data object DeleteConfirmed : ActivityFormUiIntent

    data object DeleteDismissed : ActivityFormUiIntent

    data object BackClicked : ActivityFormUiIntent
}
