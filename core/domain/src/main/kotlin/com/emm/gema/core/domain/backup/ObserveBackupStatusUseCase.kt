package com.emm.gema.core.domain.backup

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Clock
import java.time.Duration
import java.time.Instant

data class BackupStatus(
    val lastBackupAt: Instant?,
    val daysSinceLastBackup: Int?,
    val reminderThresholdDays: Int,
    val isReminderDue: Boolean,
)

class ObserveBackupStatusUseCase(
    private val settings: BackupSettingsRepository,
    private val clock: Clock,
) {

    operator fun invoke(): Flow<BackupStatus> = settings.observeSettings().map(::toStatus)

    private fun toStatus(settings: BackupSettings): BackupStatus {
        val daysSinceLastBackup: Int? = settings.lastBackupAt?.let(::daysSince)
        return BackupStatus(
            lastBackupAt = settings.lastBackupAt,
            daysSinceLastBackup = daysSinceLastBackup,
            reminderThresholdDays = settings.reminderThresholdDays,
            isReminderDue = daysSinceLastBackup == null ||
                daysSinceLastBackup >= settings.reminderThresholdDays,
        )
    }

    private fun daysSince(lastBackupAt: Instant): Int =
        Duration.between(lastBackupAt, clock.instant()).toDays().toInt()
}
