package com.emm.gema.core.domain.evaluation

class FakeSummaryDocuments : SummaryDocuments {

    val written: MutableMap<String, ByteArray> = mutableMapOf()

    override suspend fun write(fileName: String, bytes: ByteArray): SummaryFile {
        written[fileName] = bytes
        return SummaryFile(name = fileName, path = "/fake/$fileName")
    }
}
