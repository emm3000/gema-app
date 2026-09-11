package com.emm.gema.core.domain.siagie

interface SiagieDocuments {

    suspend fun nameOf(uri: String): String

    suspend fun readContent(uri: String): ByteArray
}
