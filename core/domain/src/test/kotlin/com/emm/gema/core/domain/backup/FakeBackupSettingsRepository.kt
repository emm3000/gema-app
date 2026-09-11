package com.emm.gema.core.domain.backup

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.Instant

class FakeBackupSettingsRepository(
    lastBackupAt: Instant? = null,
    reminderThresholdDays: Int = DEFAULT_REMINDER_THRESHOLD_DAYS,
) : BackupSettingsRepository {

    private val settings: MutableStateFlow<BackupSettings> = MutableStateFlow(
        BackupSettings(lastBackupAt = lastBackupAt, reminderThresholdDays = reminderThresholdDays)
    )

    override fun observeSettings(): Flow<BackupSettings> = settings

    override suspend fun setLastBackupAt(instant: Instant) {
        settings.value = settings.value.copy(lastBackupAt = instant)
    }

    override suspend fun setReminderThresholdDays(days: Int) {
        settings.value = settings.value.copy(reminderThresholdDays = days)
    }
}
