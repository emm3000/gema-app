package com.emm.gema.feature.setup

import com.emm.gema.core.domain.schoolyear.SchoolYearId
import com.emm.gema.feature.setup.periods.PeriodsViewModel
import com.emm.gema.feature.setup.section.SetupSectionViewModel
import com.emm.gema.feature.setup.year.SchoolYearDraft
import com.emm.gema.feature.setup.year.SetupYearViewModel
import com.emm.gema.feature.setup.years.SchoolYearsViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val setupModule: Module = module {
    single<SetupDraftStore> { SetupDraftStore() }
    viewModel { SetupYearViewModel(get()) }
    viewModel { (draft: SchoolYearDraft) -> SetupSectionViewModel(draft, get()) }
    viewModel { (schoolYearId: SchoolYearId) -> PeriodsViewModel(schoolYearId, get(), get(), get(), get()) }
    viewModel { SchoolYearsViewModel(get(), get(), get(), get()) }
}
