package com.emm.gema.feature.backup

sealed interface BackupUiEffect {

    data class ShareFile(val path: String, val mimeType: String) : BackupUiEffect

    data class OpenDocumentPicker(val mimeTypes: List<String>) : BackupUiEffect

    data class ShowMessage(val message: BackupMessage) : BackupUiEffect

    data object RestartApp : BackupUiEffect

    data object NavigateBack : BackupUiEffect
}
