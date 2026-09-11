package com.emm.gema.feature.evaluation

import com.emm.gema.core.domain.curriculum.CompetencyId
import com.emm.gema.core.domain.schoolyear.PeriodId
import com.emm.gema.core.domain.section.Area
import com.emm.gema.core.domain.section.SectionId
import com.emm.gema.core.domain.student.StudentId
import com.emm.gema.feature.evaluation.levels.PeriodLevelCellKey
import com.emm.gema.feature.evaluation.levels.PeriodLevelsViewModel
import com.emm.gema.feature.evaluation.worked.WorkedCompetenciesViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val evaluationModule: Module = module {
    viewModel { (sectionId: SectionId, periodId: PeriodId, area: Area) ->
        WorkedCompetenciesViewModel(sectionId, periodId, area, get(), get(), get(), get(), get(), get())
    }
    viewModel { (sectionId: SectionId, studentId: StudentId?, competencyId: CompetencyId?) ->
        PeriodLevelsViewModel(
            sectionId,
            initialCellOf(studentId, competencyId),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
        )
    }
}

private fun initialCellOf(studentId: StudentId?, competencyId: CompetencyId?): PeriodLevelCellKey? =
    if (studentId != null && competencyId != null) {
        PeriodLevelCellKey(studentId = studentId, competencyId = competencyId)
    } else {
        null
    }
