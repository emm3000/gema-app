package com.emm.gema.core.domain.evaluation

import com.emm.gema.core.domain.curriculum.Competency
import com.emm.gema.core.domain.curriculum.CompetencyId
import com.emm.gema.core.domain.curriculum.WorkedCompetencyRepository
import com.emm.gema.core.domain.schoolyear.PeriodId
import com.emm.gema.core.domain.section.Area
import com.emm.gema.core.domain.section.SectionAreaRepository
import com.emm.gema.core.domain.section.SectionId
import com.emm.gema.core.domain.student.Student
import com.emm.gema.core.domain.student.StudentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetMissingPeriodLevelCountUseCase(
    private val sectionAreaRepository: SectionAreaRepository,
    private val workedCompetencyRepository: WorkedCompetencyRepository,
    private val studentRepository: StudentRepository,
    private val periodLevelRepository: PeriodLevelRepository,
) {

    operator fun invoke(sectionId: SectionId, periodId: PeriodId): Flow<Int> = combine(
        sectionAreaRepository.observeHiddenAreas(sectionId),
        workedCompetencyRepository.observeWorked(sectionId = sectionId, periodId = periodId),
        studentRepository.observeBySection(sectionId),
        periodLevelRepository.observeRecordedCountsByPeriod(sectionId = sectionId, periodId = periodId),
    ) { hidden: Set<Area>, worked: Set<CompetencyId>, students: List<Student>, counts: Map<CompetencyId, Int> ->
        val visible: Set<CompetencyId> = worked.filter { Competency.areaOf(it) !in hidden }.toSet()
        val activeStudents: Int = students.count { !it.isWithdrawn }
        val recorded: Int = visible.sumOf { counts[it] ?: 0 }

        visible.size * activeStudents - recorded
    }
}
