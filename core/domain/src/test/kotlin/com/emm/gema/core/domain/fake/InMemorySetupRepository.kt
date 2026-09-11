package com.emm.gema.core.domain.fake

import com.emm.gema.core.domain.schoolyear.Period
import com.emm.gema.core.domain.schoolyear.SchoolYear
import com.emm.gema.core.domain.section.Section
import com.emm.gema.core.domain.setup.SetupRepository

class InMemorySetupRepository(
    private val schoolYearRepository: InMemorySchoolYearRepository,
    private val periodRepository: InMemoryPeriodRepository,
    private val sectionRepository: InMemorySectionRepository,
    private val activeSchoolYearRepository: InMemoryActiveSchoolYearRepository,
) : SetupRepository {

    override suspend fun saveAndActivate(schoolYear: SchoolYear, periods: List<Period>, section: Section) {
        schoolYearRepository.save(schoolYear)
        periodRepository.saveAll(periods)
        sectionRepository.save(section)
        activeSchoolYearRepository.activate(schoolYear.id)
    }
}
