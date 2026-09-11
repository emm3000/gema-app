package com.emm.gema.core.domain.siagie

import com.emm.gema.core.domain.section.Area
import com.emm.gema.core.domain.student.StudentCode

data class SiagieGradeEntry(
    val area: Area,
    val siagieOrdinal: Int,
    val studentCode: StudentCode,
    val achievementValue: String,
    val descriptiveConclusion: String,
) {
    init {
        require(achievementValue.isNotBlank()) { "A grade entry writes an achievement level or a comment" }
    }
}
