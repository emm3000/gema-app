package com.emm.gema.data.di

import com.emm.gema.GemaDb
import com.emm.gema.data.local.DefaultAttendanceRepository
import com.emm.gema.data.local.DefaultCourseRepository
import com.emm.gema.data.local.DefaultCourseStudentRepository
import com.emm.gema.data.local.DefaultEvaluationRepository
import com.emm.gema.data.local.DefaultStudentRepository
import com.emm.gema.domain.attendance.AttendanceRepository
import com.emm.gema.domain.course.CourseRepository
import com.emm.gema.domain.course.CourseStudentRepository
import com.emm.gema.domain.evaluation.EvaluationRepository
import com.emm.gema.domain.student.StudentRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val repositoryModule = module {

    single { provideSqlDriver(androidContext()) }
    single<GemaDb> { provideDb(get()) }

    factory<CourseRepository> {
        DefaultCourseRepository(get<GemaDb>().courseQueries)
    }

    factory<StudentRepository> {
        DefaultStudentRepository(
            get<GemaDb>().studentQueries,
            get<GemaDb>().studentCourseQueries
        )
    }

    factory<CourseStudentRepository> {
        DefaultCourseStudentRepository(
            get<GemaDb>().studentCourseQueries
        )
    }

    factory<AttendanceRepository> {
        DefaultAttendanceRepository(
            get<GemaDb>().attendanceQueries,
            get<GemaDb>().studentCourseQueries
        )
    }

    factory<EvaluationRepository> {
        DefaultEvaluationRepository(
            get<GemaDb>().evaluationQueries
        )
    }

}