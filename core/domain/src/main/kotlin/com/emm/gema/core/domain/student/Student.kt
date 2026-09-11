package com.emm.gema.core.domain.student

import com.emm.gema.core.domain.section.SectionId
import java.time.LocalDate

data class Student(
    val id: StudentId,
    val sectionId: SectionId,
    val code: StudentCode,
    val fullName: String,
    val siagieId: String? = null,
    val withdrawalDate: LocalDate? = null,
) {
    init {
        require(fullName.isNotBlank()) { "A student needs a name" }
    }

    val isWithdrawn: Boolean get() = withdrawalDate != null
}
