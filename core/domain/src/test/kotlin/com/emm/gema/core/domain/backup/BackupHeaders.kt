package com.emm.gema.core.domain.backup

import java.nio.ByteBuffer

private const val USER_VERSION_POSITION = 60
private const val SQLITE_FILE_MAGIC = "SQLite format 3"

fun sqliteContent(schemaVersion: Int, body: String = "rows"): ByteArray {
    val header: ByteBuffer = ByteBuffer.allocate(BACKUP_HEADER_BYTES)
    header.put(SQLITE_FILE_MAGIC.toByteArray(Charsets.US_ASCII))
    header.put(0)
    header.putInt(USER_VERSION_POSITION, schemaVersion)
    return header.array() + body.toByteArray(Charsets.UTF_8)
}
