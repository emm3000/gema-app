package com.emm.gema.core.domain.backup

import java.io.ByteArrayInputStream
import java.io.InputStream

class FakeBackupDocuments(
    private val name: String,
    private val content: ByteArray,
) : BackupDocuments {

    override suspend fun nameOf(uri: String): String = name

    override suspend fun readHeader(uri: String): ByteArray =
        content.copyOfRange(0, minOf(BACKUP_HEADER_BYTES, content.size))

    override suspend fun openInput(uri: String): InputStream = ByteArrayInputStream(content)
}
