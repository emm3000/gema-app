package com.emm.gema.core.domain.section

import com.emm.gema.core.domain.activity.GetActivitiesUseCase
import com.emm.gema.core.domain.evaluation.GetMissingPeriodLevelCountUseCase
import com.emm.gema.core.domain.schoolyear.PeriodId
import com.emm.gema.core.domain.siagie.ImportedTemplateKind
import com.emm.gema.core.domain.siagie.SiagieImportStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetSectionDetailExtrasUseCase(
    private val getMissingPeriodLevelCount: GetMissingPeriodLevelCountUseCase,
    private val getActivities: GetActivitiesUseCase,
    private val siagieImportStore: SiagieImportStore,
) {

    fun missingPeriodLevelCount(sectionId: SectionId, periodId: PeriodId): Flow<Int> =
        getMissingPeriodLevelCount(sectionId = sectionId, periodId = periodId)

    fun activityCount(sectionId: SectionId, periodId: PeriodId): Flow<Int> =
        getActivities(sectionId, periodId).map { activities -> activities.size }

    suspend fun hasStoredTemplate(sectionId: SectionId): Boolean =
        siagieImportStore.findTemplate(sectionId, ImportedTemplateKind.GRADES) != null
}
