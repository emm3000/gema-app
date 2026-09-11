package com.emm.gema.core.domain.backup

import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime

class CreateBackupUseCase(
    private val store: BackupStore,
    private val settings: BackupSettingsRepository,
    private val clock: Clock,
) {

    suspend operator fun invoke(): BackupFile {
        val createdAt: Instant = clock.instant()
        val file: BackupFile = store.writeSnapshot(backupFileName(LocalDateTime.now(clock)))
        settings.setLastBackupAt(createdAt)
        return file
    }
}
