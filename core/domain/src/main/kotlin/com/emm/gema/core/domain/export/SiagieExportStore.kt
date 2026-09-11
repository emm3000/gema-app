package com.emm.gema.core.domain.export

interface SiagieExportStore {

    suspend fun write(fileName: String, content: ByteArray): ExportedFile
}
