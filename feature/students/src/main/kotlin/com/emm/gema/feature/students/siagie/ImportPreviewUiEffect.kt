package com.emm.gema.feature.students.siagie

sealed interface ImportPreviewUiEffect {

    data object NavigateBack : ImportPreviewUiEffect

    data class ShowMessage(val text: String) : ImportPreviewUiEffect
}
