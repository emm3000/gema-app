package com.emm.gema.feat.dashboard.course

import com.emm.gema.data.course.CourseResponse

data class CourseUiState(
    val courses: List<CourseResponse> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)