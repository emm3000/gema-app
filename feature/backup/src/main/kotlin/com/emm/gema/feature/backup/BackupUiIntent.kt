package com.emm.gema.feature.backup

sealed interface BackupUiIntent {

    data object CreateBackupClicked : BackupUiIntent

    data object ChooseRestoreFileClicked : BackupUiIntent

    data class RestoreFilePicked(val uri: String) : BackupUiIntent

    data object RestoreConfirmed : BackupUiIntent

    data object RestoreDismissed : BackupUiIntent

    data class ReminderThresholdChanged(val value: String) : BackupUiIntent

    data object BackClicked : BackupUiIntent
}
