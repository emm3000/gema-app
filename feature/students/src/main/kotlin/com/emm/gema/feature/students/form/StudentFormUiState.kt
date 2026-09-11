package com.emm.gema.feature.students.form

import com.emm.gema.core.domain.student.StudentId
import java.time.LocalDate

data class StudentFormUiState(
    val isLoading: Boolean = true,
    val studentId: StudentId? = null,
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
    val sectionTitle: String = "",
    val isStudentCodeValid: Boolean = false,
)
