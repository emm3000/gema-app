package com.emm.gema.di

import com.emm.gema.domain.course.CourseCreator
import com.emm.gema.domain.course.CourseStudentAdder
import com.emm.gema.domain.course.CoursesFetcher
import com.emm.gema.domain.student.CourseStudentsFetcher
import com.emm.gema.domain.student.StudentCreator
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val domainModule = module {

    factoryOf(::CourseCreator)
    factoryOf(::CoursesFetcher)

    factoryOf(::StudentCreator)
    factoryOf(::CourseStudentsFetcher)
    factoryOf(::CourseStudentAdder)
}