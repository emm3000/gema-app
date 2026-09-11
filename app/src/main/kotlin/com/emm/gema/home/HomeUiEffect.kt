package com.emm.gema.home

sealed interface HomeUiEffect {

    data class NavigateToSectionForm(val schoolYearId: String, val sectionId: String?) : HomeUiEffect

    data class NavigateToSectionDetail(val sectionId: String) : HomeUiEffect

    data class NavigateToAttendanceDay(val sectionId: String) : HomeUiEffect

    data object NavigateToSchoolYears : HomeUiEffect

    data class NavigateToPeriods(val schoolYearId: String) : HomeUiEffect

    data object NavigateToBackup : HomeUiEffect
}
