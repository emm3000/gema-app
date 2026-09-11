package com.emm.gema.core.domain.schoolyear

@JvmInline
value class PeriodId(val value: String) {

    init {
        require(value.isNotBlank()) { "A period id is not blank" }
    }
}
