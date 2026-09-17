package com.emm.gema.home

import com.emm.gema.core.domain.section.SectionId

sealed interface HomeUiIntent {

    data class SectionClicked(val id: SectionId) : HomeUiIntent

    data class TakeAttendanceClicked(val id: SectionId) : HomeUiIntent

    data object AddSectionClicked : HomeUiIntent

    data object SchoolYearSwitcherClicked : HomeUiIntent

    data object OutOfPeriodClicked : HomeUiIntent

    data object BackupReminderClicked : HomeUiIntent

    data object AboutClicked : HomeUiIntent
}
