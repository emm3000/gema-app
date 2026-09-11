package com.emm.gema.feature.students.siagie

sealed interface ImportPreviewUiIntent {

    data class GroupToggled(val group: ImportGroup) : ImportPreviewUiIntent

    data class WithdrawalToggled(val studentId: String, val isSelected: Boolean) : ImportPreviewUiIntent

    data object ApplyClicked : ImportPreviewUiIntent

    data object CancelClicked : ImportPreviewUiIntent

    data object BackClicked : ImportPreviewUiIntent
}
