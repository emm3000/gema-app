package com.emm.gema.home

import com.emm.gema.core.domain.schoolyear.SchoolYearId
import com.emm.gema.core.domain.section.SectionId
import java.time.LocalDate

sealed interface HomeUiEffect {

    data class NavigateToSectionForm(val schoolYearId: SchoolYearId, val sectionId: SectionId?) : HomeUiEffect

    data class NavigateToSectionDetail(val sectionId: SectionId) : HomeUiEffect

    data class NavigateToAttendanceDay(val sectionId: SectionId, val date: LocalDate) : HomeUiEffect

    data object NavigateToSchoolYears : HomeUiEffect

    data class NavigateToPeriods(val schoolYearId: SchoolYearId) : HomeUiEffect

    data object NavigateToBackup : HomeUiEffect
}
