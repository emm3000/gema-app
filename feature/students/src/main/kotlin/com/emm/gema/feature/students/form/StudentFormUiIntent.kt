package com.emm.gema.feature.students.form

import java.time.LocalDate

sealed interface StudentFormUiIntent {

    data class StudentCodeChanged(val value: String) : StudentFormUiIntent

    data class FullNameChanged(val value: String) : StudentFormUiIntent

    data class WithdrawnToggled(val isWithdrawn: Boolean) : StudentFormUiIntent

    data class WithdrawalDateChanged(val value: LocalDate) : StudentFormUiIntent

    data object SaveClicked : StudentFormUiIntent

    data object BackClicked : StudentFormUiIntent
}
