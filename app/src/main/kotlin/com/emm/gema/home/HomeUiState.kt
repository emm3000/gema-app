package com.emm.gema.home

import com.emm.gema.core.domain.attendance.AttendanceDaySummary
import com.emm.gema.core.domain.schoolyear.SchoolYearId
import com.emm.gema.core.domain.section.SectionId

data class HomeUiState(
    val isLoading: Boolean = true,
    val schoolYearId: SchoolYearId? = null,
    val schoolYearLabel: String = "",
    val currentPeriodLabel: String? = null,
    val sections: List<SectionRow> = emptyList(),
    val backupReminder: BackupReminder? = null,
)

data class SectionRow(
    val id: SectionId,
    val title: String,
    val studentCount: Int,
    val attendance: AttendanceDaySummary = AttendanceDaySummary(0, 0, 0),
)

data class BackupReminder(
    val daysSinceLastBackup: Int,
    val hasEverBackedUp: Boolean,
)
