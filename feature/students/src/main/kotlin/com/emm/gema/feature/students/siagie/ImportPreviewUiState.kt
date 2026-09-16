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
    val reason: ImportRejectionReason,
    val instruction: ImportInstruction,
    val expected: String?,
    val found: String?,
    val foundLabel: ImportFoundLabel?,
)

sealed interface ImportRejectionReason {

    data object NotASiagieTemplate : ImportRejectionReason

    data object EmptyRoster : ImportRejectionReason

    data class MalformedRow(val row: Int) : ImportRejectionReason

    data object GradeMismatch : ImportRejectionReason

    data object SectionMismatch : ImportRejectionReason
}

enum class ImportInstruction {
    PICK_ANOTHER_FILE,
    FIX_FILE,
    PICK_ANOTHER_FILE_OR_OPEN_SECTION,
}

enum class ImportFoundLabel {
    GRADE,
    SECTION,
}

enum class ImportGroup {
    CREATED,
    UPDATED,
    WITHDRAWN,
}
