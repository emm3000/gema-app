package com.emm.gema.core.database.backup

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import com.emm.gema.core.domain.backup.BACKUP_HEADER_BYTES
import com.emm.gema.core.domain.backup.BackupDocuments
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream

class ContentResolverBackupDocuments(
    private val contentResolver: ContentResolver,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : BackupDocuments {

    override suspend fun nameOf(uri: String): String = withContext(dispatcher) {
        val document: Uri = Uri.parse(uri)
        val displayName: String? = contentResolver
            .query(document, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { cursor -> if (cursor.moveToFirst()) cursor.getString(0) else null }
        displayName ?: document.lastPathSegment.orEmpty()
    }

    override suspend fun readHeader(uri: String): ByteArray = withContext(dispatcher) {
        openInput(uri).use { source ->
            val header = ByteArray(BACKUP_HEADER_BYTES)
            val read: Int = source.readNBytesCompat(header)
            header.copyOfRange(0, read)
        }
    }

    override suspend fun openInput(uri: String): InputStream =
        contentResolver.openInputStream(Uri.parse(uri))
            ?: error("The picked backup file could not be opened")

    private fun InputStream.readNBytesCompat(destination: ByteArray): Int {
        var total: Int = 0
        while (total < destination.size) {
            val read: Int = read(destination, total, destination.size - total)
            if (read < 0) break
            total += read
        }
        return total
    }
}
