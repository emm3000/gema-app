package com.emm.gema.data.di

import android.content.SharedPreferences
import com.emm.gema.data.auth.AuthInterceptor
import com.emm.gema.data.auth.AuthRepository
import com.emm.gema.data.auth.DataStore
import com.emm.gema.data.auth.LoginService
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import retrofit2.Retrofit

val dataModule = module {
    single<Interceptor> { AuthInterceptor(get()) }
    single<OkHttpClient> { provideOkHttp(get()) }
    single<Retrofit> { provideRetrofit(get()) }
    single<SharedPreferences> { provideSharedPreferences(androidApplication()) }
    single<DataStore> { provideDataStore(get()) }
    single<LoginService> { provideApi<LoginService>(get()) }

    factoryOf(::AuthRepository)
}

inline fun <reified T> provideApi(retrofit: Retrofit): T {
    return retrofit.create(T::class.java)
}

