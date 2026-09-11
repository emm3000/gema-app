package com.emm.gema.core.database.backup

import android.content.Context
import android.content.SharedPreferences
import com.emm.gema.core.domain.backup.BackupSettings
import com.emm.gema.core.domain.backup.BackupSettingsRepository
import com.emm.gema.core.domain.backup.DEFAULT_REMINDER_THRESHOLD_DAYS
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Instant

private const val PREFERENCES_NAME = "gema-backup"
private const val KEY_LAST_BACKUP_AT = "lastBackupAt"
private const val KEY_REMINDER_THRESHOLD_DAYS = "reminderThresholdDays"
private const val NEVER = -1L

class SharedPreferencesBackupSettingsRepository(
    context: Context,
) : BackupSettingsRepository {

    private val preferences: SharedPreferences =
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    private val settings: MutableStateFlow<BackupSettings> = MutableStateFlow(storedSettings())

    override fun observeSettings(): Flow<BackupSettings> = settings.asStateFlow()

    override suspend fun setLastBackupAt(instant: Instant) {
        preferences.edit().putLong(KEY_LAST_BACKUP_AT, instant.toEpochMilli()).apply()
        settings.value = storedSettings()
    }

    override suspend fun setReminderThresholdDays(days: Int) {
        preferences.edit().putInt(KEY_REMINDER_THRESHOLD_DAYS, days).apply()
        settings.value = storedSettings()
    }

    private fun storedSettings(): BackupSettings {
        val lastBackupAt: Long = preferences.getLong(KEY_LAST_BACKUP_AT, NEVER)
        return BackupSettings(
            lastBackupAt = if (lastBackupAt == NEVER) null else Instant.ofEpochMilli(lastBackupAt),
            reminderThresholdDays = preferences.getInt(
                KEY_REMINDER_THRESHOLD_DAYS,
                DEFAULT_REMINDER_THRESHOLD_DAYS,
            ),
        )
    }
}
