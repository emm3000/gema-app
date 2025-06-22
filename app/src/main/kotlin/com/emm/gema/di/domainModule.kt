package com.emm.gema.di

import com.emm.gema.domain.attendance.AttendanceStatusFetcher
import com.emm.gema.domain.attendance.AttendanceUpdater
import com.emm.gema.domain.course.CourseCreator
import com.emm.gema.domain.course.CourseStudentAdder
import com.emm.gema.domain.course.CoursesFetcher
import com.emm.gema.domain.evaluation.CourseEvaluationsFetcher
import com.emm.gema.domain.evaluation.EvaluationCreator
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
    factoryOf(::AttendanceUpdater)

    factoryOf(::AttendanceStatusFetcher)
    factoryOf(::CourseEvaluationsFetcher)
    factoryOf(::EvaluationCreator)
}