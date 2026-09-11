package com.emm.gema.core.domain.id

fun interface IdGenerator {
    fun newId(): String
}
