package com.emm.gema.feat.dashboard.course

import java.time.LocalDate

data class CourseFormUiState(
    val courseName: String = String(),
    val grade: String = gradeOptions.firstOrNull().orEmpty(),
    val section: String = sectionsOptions.firstOrNull().orEmpty(),
    val level: String = levelOptions.firstOrNull().orEmpty(),
    val shift: String = shiftOptions.firstOrNull().orEmpty(),
    val year: String = LocalDate.now().year.toString(),
    val error: String? = null,
    val isValidFields: Boolean = false,
    val isLoading: Boolean = false,
    val success: Boolean = false,
)