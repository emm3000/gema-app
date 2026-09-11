package com.emm.gema.core.domain.backup

import java.nio.ByteBuffer

private const val SQLITE_FILE_MAGIC = "SQLite format 3"
private const val USER_VERSION_POSITION = 60

class ValidateBackupUseCase(
    private val supportedSchemaVersion: Int,
) {

    operator fun invoke(header: ByteArray): BackupValidation {
        if (!isSqliteDatabase(header)) return BackupValidation.NotABackup

        val schemaVersion: Int = ByteBuffer.wrap(header).getInt(USER_VERSION_POSITION)
        if (schemaVersion > supportedSchemaVersion) {
            return BackupValidation.FromANewerApp(
                schemaVersion = schemaVersion,
                supportedSchemaVersion = supportedSchemaVersion,
            )
        }
        return BackupValidation.Valid(schemaVersion)
    }

    private fun isSqliteDatabase(header: ByteArray): Boolean {
        if (header.size < BACKUP_HEADER_BYTES) return false

        val magic: ByteArray = SQLITE_FILE_MAGIC.toByteArray(Charsets.US_ASCII)
        val prefix: ByteArray = header.copyOfRange(0, magic.size)
        return prefix.contentEquals(magic) && header[magic.size] == ZERO_BYTE
    }

    private companion object {
        const val ZERO_BYTE: Byte = 0
    }
}
