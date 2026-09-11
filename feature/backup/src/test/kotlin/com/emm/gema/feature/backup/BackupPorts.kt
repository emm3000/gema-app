package com.emm.gema.feature.backup

import com.emm.gema.core.domain.backup.BACKUP_HEADER_BYTES
import com.emm.gema.core.domain.backup.BackupDocuments
import com.emm.gema.core.domain.backup.BackupFile
import com.emm.gema.core.domain.backup.BackupSettings
import com.emm.gema.core.domain.backup.BackupSettingsRepository
import com.emm.gema.core.domain.backup.BackupStore
import com.emm.gema.core.domain.backup.DEFAULT_REMINDER_THRESHOLD_DAYS
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.nio.ByteBuffer
import java.time.Instant

private const val USER_VERSION_POSITION = 60
private const val SQLITE_FILE_MAGIC = "SQLite format 3"

fun sqliteContent(schemaVersion: Int): ByteArray {
    val header: ByteBuffer = ByteBuffer.allocate(BACKUP_HEADER_BYTES)
    header.put(SQLITE_FILE_MAGIC.toByteArray(Charsets.US_ASCII))
    header.put(0)
    header.putInt(USER_VERSION_POSITION, schemaVersion)
    return header.array()
}

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

class FakeBackupStore : BackupStore {

    var replacedDatabase: Boolean = false
        private set

    override suspend fun writeSnapshot(fileName: String): BackupFile =
        BackupFile(name = fileName, path = "/cache/backups/$fileName")

    override suspend fun replaceDatabase(source: InputStream) {
        source.readBytes()
        replacedDatabase = true
    }
}

class FakeBackupDocuments(
    private val name: String,
    private val content: ByteArray,
) : BackupDocuments {

    override suspend fun nameOf(uri: String): String = name

    override suspend fun readHeader(uri: String): ByteArray =
        content.copyOfRange(0, minOf(BACKUP_HEADER_BYTES, content.size))

    override suspend fun openInput(uri: String): InputStream = ByteArrayInputStream(content)
}
