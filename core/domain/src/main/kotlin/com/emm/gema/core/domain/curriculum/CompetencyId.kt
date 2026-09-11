package com.emm.gema.core.domain.curriculum

@JvmInline
value class CompetencyId(val value: String) {

    init {
        require(value.isNotBlank()) { "A competency id is not blank" }
    }
}
