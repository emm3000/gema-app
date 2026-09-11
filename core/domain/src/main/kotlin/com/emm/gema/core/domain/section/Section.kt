package com.emm.gema.core.domain.section

data class Section(
    val id: String,
    val schoolYearId: String,
    val grade: Grade,
    val name: String,
) {
    init {
        require(id.isNotBlank()) { "A section needs an id" }
        require(schoolYearId.isNotBlank()) { "A section belongs to a school year" }
        require(name.isNotBlank()) { "A section needs a name" }
    }
}
