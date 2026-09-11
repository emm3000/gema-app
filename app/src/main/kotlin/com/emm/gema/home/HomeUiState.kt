package com.emm.gema.home

data class HomeUiState(
    val isLoading: Boolean = true,
    val schoolYearId: String? = null,
    val schoolYearLabel: String = "",
    val currentPeriodLabel: String? = null,
    val sections: List<SectionRow> = emptyList(),
    val backupReminder: BackupReminder? = null,
)

data class SectionRow(
    val id: String,
    val title: String,
    val studentCount: Int,
    val attendanceSummary: String = "",
)

data class BackupReminder(
    val daysSinceLastBackup: Int,
    val hasEverBackedUp: Boolean,
)
