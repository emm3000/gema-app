package com.emm.gema.feature.sections

import com.emm.gema.feature.sections.areas.SectionAreasViewModel
import com.emm.gema.feature.sections.detail.SectionDetailViewModel
import com.emm.gema.feature.sections.form.SectionFormViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val sectionsModule: Module = module {
    viewModel { (schoolYearId: String, sectionId: String?) ->
        SectionFormViewModel(schoolYearId, sectionId, get(), get(), get(), get(), get(), get())
    }
    viewModel { (sectionId: String) -> SectionAreasViewModel(sectionId, get(), get(), get(), get()) }
    viewModel { (sectionId: String) ->
        SectionDetailViewModel(sectionId, get(), get(), get(), get(), get(), get(), get())
    }
}
