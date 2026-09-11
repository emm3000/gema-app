package com.emm.gema.core.domain.evaluation

import com.emm.gema.core.domain.curriculum.Competency
import com.emm.gema.core.domain.curriculum.CompetencyId
import com.emm.gema.core.domain.curriculum.CompetencyRepository
import com.emm.gema.core.domain.curriculum.WorkedCompetencyRepository
import com.emm.gema.core.domain.schoolyear.PeriodId
import com.emm.gema.core.domain.section.Area
import com.emm.gema.core.domain.section.SectionAreaRepository
import com.emm.gema.core.domain.section.SectionId
import com.emm.gema.core.domain.student.Student
import com.emm.gema.core.domain.student.StudentRepository
import com.emm.gema.core.domain.student.orderedByName
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetPeriodLevelSummaryUseCase(
    private val sectionAreaRepository: SectionAreaRepository,
    private val competencyRepository: CompetencyRepository,
    private val workedCompetencyRepository: WorkedCompetencyRepository,
    private val studentRepository: StudentRepository,
    private val periodLevelRepository: PeriodLevelRepository,
) {

    operator fun invoke(sectionId: SectionId, periodId: PeriodId): Flow<PeriodLevelSummary> = combine(
        sectionAreaRepository.observeHiddenAreas(sectionId),
        workedCompetencyRepository.observeWorked(sectionId = sectionId, periodId = periodId),
        studentRepository.observeBySection(sectionId),
        periodLevelRepository.observeByPeriod(sectionId = sectionId, periodId = periodId),
    ) { hidden: Set<Area>, worked: Set<CompetencyId>, students: List<Student>, levels: List<PeriodLevel> ->
        val recorded: Map<PeriodLevelKey, PeriodLevel> = levels.associateBy { it.key }
        val activeStudents: List<Student> = students.filter { !it.isWithdrawn }.orderedByName()

        val areas: List<PeriodLevelAreaSummary> = Area.entries
            .filter { it !in hidden }
            .mapNotNull { area ->
                val columns: List<Competency> = competencyRepository.findByArea(area).filter { it.id in worked }
                if (columns.isEmpty()) {
                    null
                } else {
                    PeriodLevelAreaSummary(
                        area = area,
                        grid = PeriodLevelGrid(
                            columns = columns,
                            rows = activeStudents.map { student ->
                                PeriodLevelGridRow(
                                    student = student,
                                    cells = columns.map { column ->
                                        val key: PeriodLevelKey = PeriodLevelKey(
                                            sectionId = sectionId,
                                            periodId = periodId,
                                            studentId = student.id,
                                            competencyId = column.id,
                                        )
                                        recorded[key] ?: PeriodLevel(key)
                                    },
                                )
                            },
                        ),
                    )
                }
            }

        PeriodLevelSummary(areas = areas)
    }
}
