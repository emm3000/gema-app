package com.emm.gema.core.database.siagie

import com.emm.gema.core.domain.export.ExportedFile
import com.emm.gema.core.domain.export.SiagieExportStore
import java.io.File
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CacheSiagieExportStore(
    private val directory: File,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : SiagieExportStore {

    override suspend fun write(fileName: String, content: ByteArray): ExportedFile = withContext(dispatcher) {
        directory.mkdirs()
        val target = File(directory, fileName)
        target.writeBytes(content)
        ExportedFile(name = target.name, path = target.absolutePath)
    }
}
