package com.emm.gema.core.domain.evaluation

data class PeriodLevelKey(
    val sectionId: String,
    val periodId: String,
    val studentId: String,
    val competencyId: String,
) {
    init {
        require(sectionId.isNotBlank()) { "A period level belongs to a section" }
        require(periodId.isNotBlank()) { "A period level belongs to a period" }
        require(studentId.isNotBlank()) { "A period level belongs to a student" }
        require(competencyId.isNotBlank()) { "A period level belongs to a competency" }
    }
}
