package com.emm.gema.feat.dashboard.forms.courseform

sealed interface CourseFormAction {

    class OnCourseNameChange(val value: String) : CourseFormAction

    class OnGradeChange(val value: String) : CourseFormAction

    class OnSectionChange(val value: String) : CourseFormAction

    class OnLevelChange(val value: String) : CourseFormAction

    class OnShiftChange(val value: String) : CourseFormAction

    object ClearErrorDialog : CourseFormAction

    object OnSave : CourseFormAction
}