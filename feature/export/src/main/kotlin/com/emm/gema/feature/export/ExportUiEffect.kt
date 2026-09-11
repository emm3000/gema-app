package com.emm.gema.feature.export

sealed interface ExportUiEffect {

    data class ShareFile(val path: String, val mimeType: String) : ExportUiEffect

    data class NavigateToPeriodLevels(val sectionId: String) : ExportUiEffect

    data class NavigateToStudents(val sectionId: String) : ExportUiEffect

    data class ShowMessage(val message: ExportMessage) : ExportUiEffect

    data object NavigateBack : ExportUiEffect
}

enum class ExportMessage {
    EXPORT_FAILED,
    EXPORT_UNAVAILABLE,
}
