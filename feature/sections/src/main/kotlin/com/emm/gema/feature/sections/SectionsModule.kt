package com.emm.gema.feature.sections

import com.emm.gema.core.domain.schoolyear.SchoolYearId
import com.emm.gema.core.domain.section.SectionId
import com.emm.gema.feature.sections.areas.SectionAreasViewModel
import com.emm.gema.feature.sections.detail.SectionDetailViewModel
import com.emm.gema.feature.sections.form.SectionFormViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val sectionsModule: Module = module {
    viewModel { (schoolYearId: SchoolYearId, sectionId: SectionId?) ->
        SectionFormViewModel(schoolYearId, sectionId, get(), get(), get(), get(), get(), get())
    }
    viewModel { (sectionId: SectionId) -> SectionAreasViewModel(sectionId, get(), get(), get(), get()) }
    viewModel { (sectionId: SectionId) ->
        SectionDetailViewModel(sectionId, get(), get(), get(), get(), get(), get(), get())
    }
}
