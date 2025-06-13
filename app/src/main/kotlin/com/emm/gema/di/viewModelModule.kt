package com.emm.gema.di

import com.emm.gema.feat.auth.LoginViewModel
import com.emm.gema.feat.auth.RegisterViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val viewModelModule = module {
    viewModelOf(::LoginViewModel)
    viewModelOf(::RegisterViewModel)
}