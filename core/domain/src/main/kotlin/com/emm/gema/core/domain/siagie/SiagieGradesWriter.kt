package com.emm.gema.core.domain.siagie

interface SiagieGradesWriter {

    fun write(template: ByteArray, entries: List<SiagieGradeEntry>): ByteArray
}
