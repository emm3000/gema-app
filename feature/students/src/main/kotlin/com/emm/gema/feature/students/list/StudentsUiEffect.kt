package com.emm.gema.feature.students.list

sealed interface StudentsUiEffect {

    data class NavigateToStudentForm(val sectionId: String, val studentId: String?) : StudentsUiEffect

    data class OpenDocumentPicker(val mimeTypes: List<String>) : StudentsUiEffect

    data class NavigateToImportPreview(val sectionId: String, val uri: String) : StudentsUiEffect

    data object NavigateBack : StudentsUiEffect

    data class ShowMessage(val message: StudentsMessage) : StudentsUiEffect
}
