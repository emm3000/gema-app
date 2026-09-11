package com.emm.gema.core.domain.backup

import java.io.InputStream

interface BackupStore {

    suspend fun writeSnapshot(fileName: String): BackupFile

    suspend fun replaceDatabase(source: InputStream)
}
