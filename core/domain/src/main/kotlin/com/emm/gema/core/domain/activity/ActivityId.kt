package com.emm.gema.core.domain.activity

@JvmInline
value class ActivityId(val value: String) {

    init {
        require(value.isNotBlank()) { "An activity id is not blank" }
    }
}
