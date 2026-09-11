package com.emm.gema.feature.students.form

enum class StudentFormMessage {
    SAVE_FAILED,
}

sealed interface StudentCodeError {

    data class InvalidLength(val length: Int) : StudentCodeError

    data object DuplicateCode : StudentCodeError
}

enum class FullNameError {
    BLANK,
}

enum class WithdrawalDateError {
    MISSING,
}
