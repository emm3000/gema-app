package com.emm.gema.core.domain.backup

import java.io.InputStream

const val BACKUP_HEADER_BYTES: Int = 100

interface BackupDocuments {

    suspend fun nameOf(uri: String): String

    suspend fun readHeader(uri: String): ByteArray

    suspend fun openInput(uri: String): InputStream
}
