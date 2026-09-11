package com.emm.gema.home

data class HomeUiState(
    val isLoading: Boolean = true,
    val backupReminder: BackupReminder? = null,
)

data class BackupReminder(
    val daysSinceLastBackup: Int,
    val hasEverBackedUp: Boolean,
)
