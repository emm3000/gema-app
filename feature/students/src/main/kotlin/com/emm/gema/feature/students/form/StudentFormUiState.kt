package com.emm.gema.feature.students.form

import java.time.LocalDate

data class StudentFormUiState(
    val isLoading: Boolean = true,
    val studentId: String? = null,
    val studentCode: String = "",
    val studentCodeError: StudentCodeError? = null,
    val studentCodeHint: String = "",
    val fullName: String = "",
    val fullNameError: FullNameError? = null,
    val isWithdrawn: Boolean = false,
    val withdrawalDate: LocalDate? = null,
    val withdrawalDateError: WithdrawalDateError? = null,
    val canSave: Boolean = false,
    val hasSiagieId: Boolean = false,
)
