package com.emm.gema.feature.evaluation

import com.emm.gema.core.domain.section.Area
import com.emm.gema.feature.evaluation.worked.WorkedCompetenciesViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val evaluationModule: Module = module {
    viewModel { (sectionId: String, periodId: String, area: Area) ->
        WorkedCompetenciesViewModel(sectionId, periodId, area, get(), get(), get(), get(), get())
    }
}
