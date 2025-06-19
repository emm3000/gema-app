package com.emm.gema.data.di

import android.content.SharedPreferences
import com.emm.gema.data.attendance.AttendanceApi
import com.emm.gema.data.attendance.AttendanceRepository
import com.emm.gema.data.auth.AuthApi
import com.emm.gema.data.auth.AuthInterceptor
import com.emm.gema.data.auth.AuthRepository
import com.emm.gema.data.auth.DataStore
import com.emm.gema.data.course.CourseApi
import com.emm.gema.data.course.CourseRepository
import com.emm.gema.data.student.StudentApi
import com.emm.gema.data.student.StudentRepository
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import retrofit2.Retrofit

val dataModule = module {
    single<Interceptor> { AuthInterceptor(get()) }
    single<OkHttpClient> { provideOkHttp(get(), androidApplication()) }
    single<Retrofit> { provideRetrofit(get()) }
    single<SharedPreferences> { provideSharedPreferences(androidApplication()) }
    single<DataStore> { provideDataStore(get()) }
    single<AuthApi> { provideApi<AuthApi>(get()) }
    single<CourseApi> { provideApi<CourseApi>(get()) }
    single<AttendanceApi> { provideApi<AttendanceApi>(get()) }
    single<StudentApi> { provideApi<StudentApi>(get()) }

    factoryOf(::AuthRepository)
    factoryOf(::CourseRepository)
    factoryOf(::AttendanceRepository)
    factoryOf(::StudentRepository)
}

inline fun <reified T> provideApi(retrofit: Retrofit): T {
    return retrofit.create(T::class.java)
}

