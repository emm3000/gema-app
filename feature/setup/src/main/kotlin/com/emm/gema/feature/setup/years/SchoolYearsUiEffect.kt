package com.emm.gema.feature.setup.years

import com.emm.gema.core.domain.schoolyear.SchoolYearId

sealed interface SchoolYearsUiEffect {

    data class NavigateToPeriods(val id: SchoolYearId) : SchoolYearsUiEffect

    data object NavigateToSetupYear : SchoolYearsUiEffect

    data object NavigateBack : SchoolYearsUiEffect
}
