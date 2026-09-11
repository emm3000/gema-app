package com.emm.gema.feature.students.list

sealed interface StudentsUiIntent {

    data class QueryChanged(val value: String) : StudentsUiIntent

    data class StudentClicked(val id: String) : StudentsUiIntent

    data class ReactivateClicked(val id: String) : StudentsUiIntent

    data object AddStudentClicked : StudentsUiIntent

    data object WithdrawnSectionToggled : StudentsUiIntent

    data object BackClicked : StudentsUiIntent
}
