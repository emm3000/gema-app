package com.emm.gema.core.domain.evaluation

interface SummaryDocuments {

    suspend fun write(fileName: String, bytes: ByteArray): SummaryFile
}
