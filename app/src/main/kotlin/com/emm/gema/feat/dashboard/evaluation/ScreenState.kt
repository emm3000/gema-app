package com.emm.gema.feat.dashboard.evaluation

sealed interface ScreenState {

    data class EmptyCourses(val message: String) : ScreenState

    data class EmptyEvaluations(val message: String) : ScreenState

    object None : ScreenState
}