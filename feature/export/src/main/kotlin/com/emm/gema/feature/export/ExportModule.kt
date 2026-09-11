package com.emm.gema.feature.export

import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val exportModule: Module = module {
    viewModel { (sectionId: String) ->
        ExportViewModel(sectionId, get(), get(), get(), get(), get(), get(), get(), get())
    }
}
