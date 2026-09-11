package com.emm.gema.core.domain.activity

import java.time.LocalDate

data class Activity(
    val id: String,
    val sectionId: String,
    val periodId: String,
    val name: String,
    val date: LocalDate,
    val competencyIds: Set<String>,
) {
    init {
        require(id.isNotBlank()) { "An activity needs an id" }
        require(sectionId.isNotBlank()) { "An activity belongs to a section" }
        require(periodId.isNotBlank()) { "An activity belongs to a period" }
        require(name.isNotBlank()) { "An activity needs a name" }
        require(competencyIds.isNotEmpty()) { "An activity needs at least one competency" }
    }
}
