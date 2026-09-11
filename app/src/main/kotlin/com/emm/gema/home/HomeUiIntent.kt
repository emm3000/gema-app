package com.emm.gema.home

sealed interface HomeUiIntent {

    data class SectionClicked(val id: String) : HomeUiIntent

    data object AddSectionClicked : HomeUiIntent

    data object SchoolYearSwitcherClicked : HomeUiIntent

    data object OutOfPeriodClicked : HomeUiIntent

    data object BackupReminderClicked : HomeUiIntent
}
