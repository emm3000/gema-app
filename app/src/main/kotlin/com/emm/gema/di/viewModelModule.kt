package com.emm.gema.di

import com.emm.gema.feat.auth.LoginViewModel
import com.emm.gema.feat.auth.RegisterViewModel
import com.emm.gema.feat.dashboard.DashboardViewModel
import com.emm.gema.feat.dashboard.attendance.AttendanceViewModel
import com.emm.gema.feat.dashboard.course.CourseFormViewModel
import com.emm.gema.feat.dashboard.course.CourseViewModel
import com.emm.gema.feat.dashboard.evaluation.EvaluationFormViewModel
import com.emm.gema.feat.dashboard.evaluation.EvaluationsViewModel
import com.emm.gema.feat.dashboard.student.StudentFormViewModel
import com.emm.gema.feat.dashboard.student.StudentListViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val viewModelModule = module {
    viewModelOf(::LoginViewModel)
    viewModelOf(::RegisterViewModel)
    viewModelOf(::CourseFormViewModel)
    viewModelOf(::CourseViewModel)
    viewModelOf(::AttendanceViewModel)

    viewModelOf(::EvaluationsViewModel)
    viewModelOf(::EvaluationFormViewModel)
    viewModelOf(::DashboardViewModel)

    viewModel {
        StudentFormViewModel(
            courseId = it.get(),
            studentCreator = get()
        )
    }

    viewModel {
        StudentListViewModel(
            courseId = it.get(),
            courseStudentsFetcher = get()
        )
    }
}