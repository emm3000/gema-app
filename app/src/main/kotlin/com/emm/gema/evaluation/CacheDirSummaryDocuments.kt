package com.emm.gema.evaluation

import com.emm.gema.core.domain.evaluation.SummaryDocuments
import com.emm.gema.core.domain.evaluation.SummaryFile
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class CacheDirSummaryDocuments(
    private val summariesDirectory: File,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : SummaryDocuments {

    override suspend fun write(fileName: String, bytes: ByteArray): SummaryFile = withContext(dispatcher) {
        summariesDirectory.mkdirs()
        val file = File(summariesDirectory, fileName)
        file.writeBytes(bytes)
        SummaryFile(name = file.name, path = file.absolutePath)
    }
}
