package com.emm.gema.core.domain.student

@JvmInline
value class StudentCode(val value: String) {

    init {
        require(isValid(value)) { "A student code is $LENGTH digits" }
    }

    companion object {
        const val LENGTH: Int = 14

        fun isValid(value: String): Boolean = value.length == LENGTH && value.all(Char::isDigit)

        fun orNull(value: String): StudentCode? = if (isValid(value)) StudentCode(value) else null
    }
}
