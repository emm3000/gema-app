package com.emm.gema.feature.students.siagie

import com.emm.gema.core.domain.student.StudentId

data class ImportPreviewUiState(
    val isLoading: Boolean = true,
    val fileName: String = "",
    val sectionTitle: String = "",
    val rosterSize: Int = 0,
    val rejection: ImportRejection? = null,
    val created: List<ImportStudentRow> = emptyList(),
    val updated: List<ImportStudentRow> = emptyList(),
    val proposedWithdrawals: List<ImportWithdrawalRow> = emptyList(),
    val expandedGroup: ImportGroup? = null,
    val isApplying: Boolean = false,
) {
    val canApply: Boolean
        get() = !isLoading && !isApplying && rejection == null
}

data class ImportStudentRow(
    val studentCode: String,
    val displayName: String,
)

data class ImportWithdrawalRow(
    val studentId: StudentId,
    val displayName: String,
    val isSelected: Boolean,
)

data class ImportRejection(
    val reason: String,
    val instruction: String,
    val expected: String?,
    val found: String?,
    val foundLabel: String?,
)

enum class ImportGroup {
    CREATED,
    UPDATED,
    WITHDRAWN,
}
