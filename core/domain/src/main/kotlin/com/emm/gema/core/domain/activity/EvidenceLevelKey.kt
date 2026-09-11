package com.emm.gema.core.domain.activity

data class EvidenceLevelKey(
    val activityId: String,
    val studentId: String,
    val competencyId: String,
) {
    init {
        require(activityId.isNotBlank()) { "An evidence level belongs to an activity" }
        require(studentId.isNotBlank()) { "An evidence level belongs to a student" }
        require(competencyId.isNotBlank()) { "An evidence level belongs to a competency" }
    }
}
