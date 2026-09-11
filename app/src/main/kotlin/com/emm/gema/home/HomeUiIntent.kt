package com.emm.gema.home

sealed interface HomeUiIntent {

    data object BackupReminderClicked : HomeUiIntent
}
