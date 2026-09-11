package com.emm.gema.core.domain.student

import java.time.LocalDate

data class Student(
    val id: String,
    val sectionId: String,
    val code: StudentCode,
    val fullName: String,
    val siagieId: String? = null,
    val withdrawalDate: LocalDate? = null,
) {
    init {
        require(id.isNotBlank()) { "A student needs an id" }
        require(sectionId.isNotBlank()) { "A student belongs to a section" }
        require(fullName.isNotBlank()) { "A student needs a name" }
    }

    val isWithdrawn: Boolean get() = withdrawalDate != null
}
