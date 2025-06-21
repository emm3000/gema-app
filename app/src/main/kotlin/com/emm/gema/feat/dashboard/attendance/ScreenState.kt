package com.emm.gema.feat.dashboard.attendance

sealed interface ScreenState {

    data class EmptyCourses(val message: String) : ScreenState

    data class EmptyStudents(val message: String) : ScreenState

    object None : ScreenState
}