package com.emm.gema.di

import com.emm.gema.feat.auth.LoginViewModel
import com.emm.gema.feat.auth.RegisterViewModel
import com.emm.gema.feat.dashboard.attendance.AttendanceViewModel
import com.emm.gema.feat.dashboard.course.CourseFormViewModel
import com.emm.gema.feat.dashboard.course.CourseViewModel
import com.emm.gema.feat.dashboard.forms.StudentFormViewModel
import com.emm.gema.feat.dashboard.forms.StudentListViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val viewModelModule = module {
    viewModelOf(::LoginViewModel)
    viewModelOf(::RegisterViewModel)
    viewModelOf(::CourseFormViewModel)
    viewModelOf(::CourseViewModel)
    viewModelOf(::AttendanceViewModel)

    viewModel {
        StudentFormViewModel(
            courseId = it.get(),
            repository = get()
        )
    }

    viewModel {
        StudentListViewModel(
            courseId = it.get(),
            repository = get()
        )
    }
}