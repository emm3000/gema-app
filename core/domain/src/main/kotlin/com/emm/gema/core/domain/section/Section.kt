package com.emm.gema.core.domain.section

import com.emm.gema.core.domain.schoolyear.SchoolYearId

data class Section(
    val id: SectionId,
    val schoolYearId: SchoolYearId,
    val grade: Grade,
    val name: String,
) {
    init {
        require(name.isNotBlank()) { "A section needs a name" }
    }
}
