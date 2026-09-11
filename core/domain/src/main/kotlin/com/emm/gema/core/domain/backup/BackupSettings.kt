package com.emm.gema.core.domain.backup

import java.time.Instant

const val DEFAULT_REMINDER_THRESHOLD_DAYS: Int = 7
const val MINIMUM_REMINDER_THRESHOLD_DAYS: Int = 1
const val MAXIMUM_REMINDER_THRESHOLD_DAYS: Int = 90

data class BackupSettings(
    val lastBackupAt: Instant?,
    val reminderThresholdDays: Int,
)
