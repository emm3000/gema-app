package com.emm.gema.feature.export

import com.emm.gema.core.domain.curriculum.CompetencyId
import com.emm.gema.core.domain.section.SectionId
import com.emm.gema.core.domain.student.StudentId

sealed interface ExportUiEffect {

    data class ShareFile(val path: String, val mimeType: String) : ExportUiEffect

    data class NavigateToPeriodLevelCell(
        val sectionId: SectionId,
        val studentId: StudentId,
        val competencyId: CompetencyId,
    ) : ExportUiEffect

    data class NavigateToStudents(val sectionId: SectionId) : ExportUiEffect

    data class ShowMessage(val message: ExportMessage) : ExportUiEffect

    data object NavigateBack : ExportUiEffect
}

enum class ExportMessage {
    EXPORT_FAILED,
    EXPORT_UNAVAILABLE,
}
