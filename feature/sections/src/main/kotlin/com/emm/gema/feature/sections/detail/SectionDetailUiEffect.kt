package com.emm.gema.feature.sections.detail

import java.time.LocalDate

sealed interface SectionDetailUiEffect {

    data class NavigateToAttendanceDay(val sectionId: String, val date: LocalDate) : SectionDetailUiEffect

    data class NavigateToStudents(val sectionId: String) : SectionDetailUiEffect

    data class NavigateToPeriodLevels(val sectionId: String) : SectionDetailUiEffect

    data class NavigateToExport(val sectionId: String) : SectionDetailUiEffect

    data class NavigateToActivities(val sectionId: String) : SectionDetailUiEffect

    data class NavigateToSectionAreas(val sectionId: String) : SectionDetailUiEffect

    data class NavigateToSectionForm(val schoolYearId: String, val sectionId: String) : SectionDetailUiEffect

    data object NavigateBack : SectionDetailUiEffect
}
