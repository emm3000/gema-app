package com.emm.gema.core.domain.schoolyear

@JvmInline
value class SchoolYearId(val value: String) {

    init {
        require(value.isNotBlank()) { "A school year id is not blank" }
    }
}
