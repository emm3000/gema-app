package com.emm.gema.feature.students.list

import java.time.LocalDate

data class StudentsUiState(
    val isLoading: Boolean = true,
    val sectionTitle: String = "",
    val query: String = "",
    val activeStudents: List<StudentRow> = emptyList(),
    val withdrawnStudents: List<StudentRow> = emptyList(),
    val isWithdrawnExpanded: Boolean = false,
) {
    val isEmpty: Boolean
        get() = !isLoading && query.isBlank() && activeStudents.isEmpty() && withdrawnStudents.isEmpty()
}

data class StudentRow(
    val id: String,
    val displayName: String,
    val studentCode: String,
    val withdrawalDate: LocalDate? = null,
)
