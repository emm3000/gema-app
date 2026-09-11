package com.emm.gema.feature.students.form

sealed interface StudentFormUiEffect {

    data object NavigateBack : StudentFormUiEffect

    data class ShowMessage(val text: String) : StudentFormUiEffect
}
