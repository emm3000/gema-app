package com.emm.gema.di

import com.emm.gema.domain.course.CourseCreator
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val domainModule = module {

    factoryOf(::CourseCreator)
}