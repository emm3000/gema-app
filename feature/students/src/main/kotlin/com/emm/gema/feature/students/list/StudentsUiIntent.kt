package com.emm.gema.feature.students.list

import com.emm.gema.core.domain.student.StudentId

sealed interface StudentsUiIntent {

    data class QueryChanged(val value: String) : StudentsUiIntent

    data class StudentClicked(val id: StudentId) : StudentsUiIntent

    data class ReactivateClicked(val id: StudentId) : StudentsUiIntent

    data class ImportFilePicked(val uri: String) : StudentsUiIntent

    data object AddStudentClicked : StudentsUiIntent

    data object ImportClicked : StudentsUiIntent

    data object SearchToggled : StudentsUiIntent

    data object WithdrawnSectionToggled : StudentsUiIntent

    data object BackClicked : StudentsUiIntent
}
