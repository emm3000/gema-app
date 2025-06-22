package com.emm.gema.feat.dashboard.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.gema.domain.student.CourseStudentsFetcher
import com.emm.gema.domain.student.Student
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class StudentListViewModel(
    courseId: String,
    courseStudentsFetcher: CourseStudentsFetcher,
) : ViewModel() {

    val students: StateFlow<List<Student>> = courseStudentsFetcher.fetch(courseId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

}