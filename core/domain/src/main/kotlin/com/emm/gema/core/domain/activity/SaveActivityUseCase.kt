package com.emm.gema.core.domain.activity

import com.emm.gema.core.domain.id.IdGenerator
import com.emm.gema.core.domain.schoolyear.FindPeriodForDateUseCase
import com.emm.gema.core.domain.schoolyear.Period
import com.emm.gema.core.domain.section.Section
import com.emm.gema.core.domain.section.SectionRepository
import java.time.LocalDate

class SaveActivityUseCase(
    private val sectionRepository: SectionRepository,
    private val findPeriodForDate: FindPeriodForDateUseCase,
    private val activityRepository: ActivityRepository,
    private val idGenerator: IdGenerator,
) {

    suspend operator fun invoke(
        sectionId: String,
        activityId: String?,
        name: String,
        date: LocalDate,
        competencyIds: Set<String>,
    ): Activity {
        val section: Section = requireNotNull(sectionRepository.findById(sectionId)) {
            "There is no section $sectionId"
        }
        val period: Period = requireNotNull(findPeriodForDate(section.schoolYearId, date)) {
            "No period contains $date"
        }

        val activity = Activity(
            id = activityId ?: idGenerator.newId(),
            sectionId = sectionId,
            periodId = period.id,
            name = name.trim(),
            date = date,
            competencyIds = competencyIds,
        )
        activityRepository.save(activity)
        return activity
    }
}
