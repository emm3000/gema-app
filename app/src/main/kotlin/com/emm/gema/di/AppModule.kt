package com.emm.gema.di

import com.emm.gema.core.database.GemaDb
import com.emm.gema.core.database.UuidIdGenerator
import com.emm.gema.core.database.createGemaDb
import com.emm.gema.core.database.schoolyear.SqlDelightSchoolYearRepository
import com.emm.gema.core.domain.id.IdGenerator
import com.emm.gema.core.domain.schoolyear.CreateSchoolYearUseCase
import com.emm.gema.core.domain.schoolyear.GetSchoolYearsUseCase
import com.emm.gema.core.domain.schoolyear.SchoolYearRepository
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

val appModule: Module = module {
    single<GemaDb> { createGemaDb(androidContext()) }
    single<IdGenerator> { UuidIdGenerator() }
    single<SchoolYearRepository> { SqlDelightSchoolYearRepository(get()) }
    factory<CreateSchoolYearUseCase> { CreateSchoolYearUseCase(get(), get()) }
    factory<GetSchoolYearsUseCase> { GetSchoolYearsUseCase(get()) }
}
