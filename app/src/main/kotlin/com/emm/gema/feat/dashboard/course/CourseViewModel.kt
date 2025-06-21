package com.emm.gema.feat.dashboard.course

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.gema.domain.course.CoursesFetcher
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class CourseViewModel(coursesFetcher: CoursesFetcher) : ViewModel() {

    val courses = coursesFetcher.fetchAll()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = emptyList(),
        )
}