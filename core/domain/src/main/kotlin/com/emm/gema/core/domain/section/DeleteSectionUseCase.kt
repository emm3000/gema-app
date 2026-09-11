package com.emm.gema.core.domain.section

import com.emm.gema.core.domain.attendance.AttendanceRepository
import com.emm.gema.core.domain.curriculum.WorkedCompetencyRepository
import com.emm.gema.core.domain.evaluation.PeriodLevelRepository
import com.emm.gema.core.domain.siagie.SiagieImportStore
import com.emm.gema.core.domain.student.StudentRepository

class DeleteSectionUseCase(
    private val sectionRepository: SectionRepository,
    private val sectionAreaRepository: SectionAreaRepository,
    private val workedCompetencyRepository: WorkedCompetencyRepository,
    private val studentRepository: StudentRepository,
    private val siagieImportStore: SiagieImportStore,
    private val periodLevelRepository: PeriodLevelRepository,
    private val attendanceRepository: AttendanceRepository,
) {

    suspend operator fun invoke(sectionId: String) {
        sectionRepository.delete(sectionId)
        sectionAreaRepository.clearSection(sectionId)
        workedCompetencyRepository.clearSection(sectionId)
        studentRepository.deleteBySection(sectionId)
        siagieImportStore.clearSection(sectionId)
        periodLevelRepository.clearSection(sectionId)
        attendanceRepository.deleteBySection(sectionId)
    }
}
