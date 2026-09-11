package com.emm.gema.core.domain.section

@JvmInline
value class SectionId(val value: String) {

    init {
        require(value.isNotBlank()) { "A section id is not blank" }
    }
}
