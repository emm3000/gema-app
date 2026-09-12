package com.emm.gema.feature.export

import com.emm.gema.core.domain.section.SectionId
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val exportModule: Module = module {
    viewModel { (sectionId: SectionId) ->
        ExportViewModel(
            sectionId,
            get(),
            get(),
            get(),
            get(),
            GradesExport(get(), get(), get()),
            get(),
            AttendanceExport(get(), get()),
            get(),
        )
    }
}
