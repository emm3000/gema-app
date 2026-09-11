package com.emm.gema.core.database

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.emm.gema.core.database.backup.SqliteBackupStore
import com.emm.gema.core.domain.backup.BackupStore
import java.io.File

private const val DATABASE_NAME = "gema.db"
private const val BACKUP_DIRECTORY_NAME = "backups"

class GemaDatabase(context: Context) {

    private val driver: SqlDriver = AndroidSqliteDriver(
        schema = GemaDb.Schema,
        context = context,
        name = DATABASE_NAME,
    )

    val database: GemaDb = GemaDb(driver)

    val schemaVersion: Int = GemaDb.Schema.version.toInt()

    val backupStore: BackupStore = SqliteBackupStore(
        driver = driver,
        databaseFile = context.getDatabasePath(DATABASE_NAME),
        backupDirectory = File(context.cacheDir, BACKUP_DIRECTORY_NAME),
    )
}
