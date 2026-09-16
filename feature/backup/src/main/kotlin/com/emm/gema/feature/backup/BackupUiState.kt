package com.emm.gema.feature.backup

import com.emm.gema.core.domain.backup.DEFAULT_REMINDER_THRESHOLD_DAYS
import java.time.LocalDate

data class BackupUiState(
    val isLoading: Boolean = true,
    val lastBackupDate: LocalDate? = null,
    val daysSinceLastBackup: Int? = null,
    val reminderThresholdDays: Int = DEFAULT_REMINDER_THRESHOLD_DAYS,
    val reminderThresholdInput: String = DEFAULT_REMINDER_THRESHOLD_DAYS.toString(),
    val isReminderThresholdInvalid: Boolean = false,
    val isBackupOverdue: Boolean = false,
    val isCreating: Boolean = false,
    val restoreConfirmation: RestoreConfirmation? = null,
    val isRestoring: Boolean = false,
)

data class RestoreConfirmation(
    val uri: String,
    val fileName: String,
    val currentSchoolYearCount: Int,
    val currentStudentCount: Int,
)
