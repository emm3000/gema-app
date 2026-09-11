package com.emm.gema.core.database.siagie

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import com.emm.gema.core.domain.siagie.SiagieDocuments
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ContentResolverSiagieDocuments(
    private val contentResolver: ContentResolver,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : SiagieDocuments {

    override suspend fun nameOf(uri: String): String = withContext(dispatcher) {
        val document: Uri = Uri.parse(uri)
        val displayName: String? = contentResolver
            .query(document, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { cursor -> if (cursor.moveToFirst()) cursor.getString(0) else null }
        displayName ?: document.lastPathSegment.orEmpty()
    }

    override suspend fun readContent(uri: String): ByteArray = withContext(dispatcher) {
        val source = contentResolver.openInputStream(Uri.parse(uri))
            ?: error("The picked SIAGIE template could not be opened")
        source.use { it.readBytes() }
    }
}
