package com.emm.gema.core.domain.siagie

fun interface SiagieRosterReader {

    fun read(fileName: String, content: ByteArray): SiagieRosterResult
}
