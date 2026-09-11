package com.emm.gema.feature.activities

import com.emm.gema.core.domain.activity.ActivityId
import com.emm.gema.core.domain.section.SectionId
import com.emm.gema.feature.activities.evidence.ActivityEvidenceViewModel
import com.emm.gema.feature.activities.form.ActivityFormViewModel
import com.emm.gema.feature.activities.list.ActivitiesViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val activitiesModule: Module = module {
    viewModel { (sectionId: SectionId) ->
        ActivitiesViewModel(sectionId, get(), get(), get(), get(), get(), get(), get(), get())
    }
    viewModel { (sectionId: SectionId, activityId: ActivityId?) ->
        ActivityFormViewModel(sectionId, activityId, get(), get(), get(), get(), get(), get(), get(), get())
    }
    viewModel { (activityId: ActivityId) ->
        ActivityEvidenceViewModel(activityId, get(), get(), get(), get(), get(), get(), get(), get())
    }
}
