package com.emm.gema.core.database.backup

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import com.emm.gema.core.domain.backup.BackupFile
import com.emm.gema.core.domain.backup.BackupStore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption

private const val WRITE_AHEAD_LOG_SUFFIX = "-wal"
private const val SHARED_MEMORY_SUFFIX = "-shm"
private const val INCOMING_SUFFIX = ".incoming"

class SqliteBackupStore(
    private val driver: SqlDriver,
    private val databaseFile: File,
    private val backupDirectory: File,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : BackupStore {

    override suspend fun writeSnapshot(fileName: String): BackupFile = withContext(dispatcher) {
        checkpoint()
        backupDirectory.mkdirs()
        val snapshot = File(backupDirectory, fileName)
        databaseFile.inputStream().use { source ->
            snapshot.outputStream().use { destination -> source.copyTo(destination) }
        }
        BackupFile(name = snapshot.name, path = snapshot.absolutePath)
    }

    override suspend fun replaceDatabase(source: InputStream): Unit = withContext(dispatcher) {
        val incoming = File(databaseFile.parentFile, databaseFile.name + INCOMING_SUFFIX)
        runCatching {
            incoming.outputStream().use { destination -> source.copyTo(destination) }
        }.onFailure { incoming.delete() }.getOrThrow()

        Files.move(
            incoming.toPath(),
            databaseFile.toPath(),
            StandardCopyOption.REPLACE_EXISTING,
            StandardCopyOption.ATOMIC_MOVE,
        )
        sidecarsOf(databaseFile).forEach(File::delete)
    }

    private fun checkpoint() {
        driver.executeQuery(
            identifier = null,
            sql = "PRAGMA wal_checkpoint(TRUNCATE)",
            mapper = { QueryResult.Unit },
            parameters = 0,
        ).value
    }

    private fun sidecarsOf(database: File): List<File> = listOf(
        File(database.parentFile, database.name + WRITE_AHEAD_LOG_SUFFIX),
        File(database.parentFile, database.name + SHARED_MEMORY_SUFFIX),
    )
}
