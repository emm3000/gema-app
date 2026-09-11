package com.emm.gema.feature.sections.detail

import com.emm.gema.core.domain.schoolyear.SchoolYearId
import com.emm.gema.core.domain.section.SectionId
import java.time.LocalDate

sealed interface SectionDetailUiEffect {

    data class NavigateToAttendanceDay(val sectionId: SectionId, val date: LocalDate) : SectionDetailUiEffect

    data class NavigateToStudents(val sectionId: SectionId) : SectionDetailUiEffect

    data class NavigateToPeriodLevels(val sectionId: SectionId) : SectionDetailUiEffect

    data class NavigateToExport(val sectionId: SectionId) : SectionDetailUiEffect

    data class NavigateToActivities(val sectionId: SectionId) : SectionDetailUiEffect

    data class NavigateToSectionAreas(val sectionId: SectionId) : SectionDetailUiEffect

    data class NavigateToSectionForm(val schoolYearId: SchoolYearId, val sectionId: SectionId) : SectionDetailUiEffect

    data object NavigateBack : SectionDetailUiEffect
}
