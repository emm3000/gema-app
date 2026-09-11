package com.emm.gema.core.domain.siagie

import com.emm.gema.core.domain.student.StudentCode

data class SiagieRoster(
    val gradeNumber: Int?,
    val sectionName: String?,
    val students: List<SiagieRosterStudent>,
)

data class SiagieRosterStudent(
    val siagieId: String?,
    val code: StudentCode,
    val fullName: String,
) {
    init {
        require(fullName.isNotBlank()) { "A roster student needs a name" }
    }
}
