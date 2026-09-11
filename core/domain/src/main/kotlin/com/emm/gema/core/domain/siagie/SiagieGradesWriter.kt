package com.emm.gema.core.domain.siagie

import com.emm.gema.core.domain.section.Area
import com.emm.gema.core.domain.student.StudentCode

interface SiagieGradesWriter {

    fun write(template: ByteArray, entries: List<SiagieGradeEntry>): SiagieGradesWriteResult
}

sealed interface SiagieGradesWriteResult {

    data class Written(val content: ByteArray) : SiagieGradesWriteResult

    data class Unmapped(
        val areas: List<Area>,
        val studentCodes: List<StudentCode>,
    ) : SiagieGradesWriteResult
}
