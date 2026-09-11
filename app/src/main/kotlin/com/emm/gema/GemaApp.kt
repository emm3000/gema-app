package com.emm.gema

import android.app.Application
import com.emm.gema.di.appModule
import com.emm.gema.feature.evaluation.evaluationModule
import com.emm.gema.feature.sections.sectionsModule
import com.emm.gema.feature.setup.setupModule
import com.emm.gema.feature.students.studentsModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class GemaApp : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@GemaApp)
            androidLogger()
            modules(appModule, setupModule, sectionsModule, studentsModule, evaluationModule)
        }
    }
}
