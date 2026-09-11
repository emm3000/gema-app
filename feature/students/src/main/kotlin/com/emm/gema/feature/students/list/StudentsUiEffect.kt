package com.emm.gema.feature.students.list

import com.emm.gema.core.domain.section.SectionId
import com.emm.gema.core.domain.student.StudentId

sealed interface StudentsUiEffect {

    data class NavigateToStudentForm(val sectionId: SectionId, val studentId: StudentId?) : StudentsUiEffect

    data class OpenDocumentPicker(val mimeTypes: List<String>) : StudentsUiEffect

    data class NavigateToImportPreview(val sectionId: SectionId, val uri: String) : StudentsUiEffect

    data object NavigateBack : StudentsUiEffect

    data class ShowMessage(val message: StudentsMessage) : StudentsUiEffect
}
