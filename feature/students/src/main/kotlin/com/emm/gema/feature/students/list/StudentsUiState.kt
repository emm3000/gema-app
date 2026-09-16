package com.emm.gema.feature.students.list

import com.emm.gema.core.domain.student.StudentId
import java.time.LocalDate

data class StudentsUiState(
    val isLoading: Boolean = true,
    val sectionTitle: String = "",
    val query: String = "",
    val isSearchVisible: Boolean = false,
    val activeStudents: List<StudentRow> = emptyList(),
    val withdrawnStudents: List<StudentRow> = emptyList(),
    val isWithdrawnExpanded: Boolean = false,
) {
    val isEmpty: Boolean
        get() = !isLoading && query.isBlank() && activeStudents.isEmpty() && withdrawnStudents.isEmpty()
}

data class StudentRow(
    val id: StudentId,
    val displayName: String,
    val studentCode: String,
    val withdrawalDate: LocalDate? = null,
)
