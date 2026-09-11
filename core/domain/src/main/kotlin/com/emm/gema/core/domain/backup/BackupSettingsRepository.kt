package com.emm.gema.core.domain.backup

import kotlinx.coroutines.flow.Flow
import java.time.Instant

interface BackupSettingsRepository {

    fun observeSettings(): Flow<BackupSettings>

    suspend fun setLastBackupAt(instant: Instant)

    suspend fun setReminderThresholdDays(days: Int)
}
