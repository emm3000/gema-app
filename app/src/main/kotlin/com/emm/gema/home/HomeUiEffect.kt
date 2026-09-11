package com.emm.gema.home

import java.time.LocalDate

sealed interface HomeUiEffect {

    data class NavigateToSectionForm(val schoolYearId: String, val sectionId: String?) : HomeUiEffect

    data class NavigateToSectionDetail(val sectionId: String) : HomeUiEffect

    data class NavigateToAttendanceDay(val sectionId: String, val date: LocalDate) : HomeUiEffect

    data object NavigateToSchoolYears : HomeUiEffect

    data class NavigateToPeriods(val schoolYearId: String) : HomeUiEffect

    data object NavigateToBackup : HomeUiEffect
}
