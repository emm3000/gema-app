package com.emm.gema.core.domain.student

@JvmInline
value class StudentId(val value: String) {

    init {
        require(value.isNotBlank()) { "A student id is not blank" }
    }
}
