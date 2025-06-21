package com.emm.gema.data.di

import com.emm.gema.GemaDb
import com.emm.gema.data.local.DefaultCourseRepository
import com.emm.gema.domain.course.CourseRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val repositoryModule = module {

    single { provideSqlDriver(androidContext()) }
    single<GemaDb> { provideDb(get()) }

    factory<CourseRepository> {
        DefaultCourseRepository(get<GemaDb>().courseQueries)
    }
}