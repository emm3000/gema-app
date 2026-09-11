package com.emm.gema.core.domain.fake

import com.emm.gema.core.domain.activity.ActivityRepository
import com.emm.gema.core.domain.activity.EvidenceLevelRepository
import com.emm.gema.core.domain.attendance.AttendanceRepository
import com.emm.gema.core.domain.curriculum.WorkedCompetencyRepository
import com.emm.gema.core.domain.evaluation.PeriodLevelRepository
import com.emm.gema.core.domain.section.SectionAreaRepository
import com.emm.gema.core.domain.section.SectionCascade
import com.emm.gema.core.domain.section.SectionRepository
import com.emm.gema.core.domain.siagie.SiagieImportStore
import com.emm.gema.core.domain.student.StudentRepository

class InMemorySectionCascade(
    private val sectionRepository: SectionRepository,
    private val sectionAreaRepository: SectionAreaRepository,
    private val workedCompetencyRepository: WorkedCompetencyRepository,
    private val studentRepository: StudentRepository,
    private val siagieImportStore: SiagieImportStore,
    private val periodLevelRepository: PeriodLevelRepository,
    private val attendanceRepository: AttendanceRepository,
    private val activityRepository: ActivityRepository,
    private val evidenceLevelRepository: EvidenceLevelRepository,
) : SectionCascade {

    override suspend fun deleteSection(sectionId: String) {
        sectionRepository.delete(sectionId)
        sectionAreaRepository.clearSection(sectionId)
        workedCompetencyRepository.clearSection(sectionId)
        studentRepository.deleteBySection(sectionId)
        siagieImportStore.clearSection(sectionId)
        periodLevelRepository.clearSection(sectionId)
        attendanceRepository.deleteBySection(sectionId)
        evidenceLevelRepository.clearSection(sectionId)
        activityRepository.clearSection(sectionId)
    }
}
