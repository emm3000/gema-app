package com.emm.gema.core.domain.backup

import java.io.InputStream

class FakeBackupStore : BackupStore {

    var snapshotCount: Int = 0
        private set

    var replacedWith: String? = null
        private set

    override suspend fun writeSnapshot(fileName: String): BackupFile {
        snapshotCount += 1
        return BackupFile(name = fileName, path = "/backups/$fileName")
    }

    override suspend fun replaceDatabase(source: InputStream) {
        replacedWith = source.readBytes().decodeToString()
    }
}
