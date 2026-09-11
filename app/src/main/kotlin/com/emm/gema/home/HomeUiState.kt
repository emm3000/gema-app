package com.emm.gema.home

import com.emm.gema.core.domain.attendance.AttendanceDaySummary

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
    val attendance: AttendanceDaySummary = AttendanceDaySummary(0, 0, 0),
)

data class BackupReminder(
    val daysSinceLastBackup: Int,
    val hasEverBackedUp: Boolean,
)
