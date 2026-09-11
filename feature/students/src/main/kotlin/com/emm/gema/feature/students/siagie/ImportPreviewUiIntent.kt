package com.emm.gema.feature.students.siagie

import com.emm.gema.core.domain.student.StudentId

sealed interface ImportPreviewUiIntent {

    data class GroupToggled(val group: ImportGroup) : ImportPreviewUiIntent

    data class WithdrawalToggled(val studentId: StudentId, val isSelected: Boolean) : ImportPreviewUiIntent

    data object ApplyClicked : ImportPreviewUiIntent

    data object CancelClicked : ImportPreviewUiIntent

    data object BackClicked : ImportPreviewUiIntent
}
