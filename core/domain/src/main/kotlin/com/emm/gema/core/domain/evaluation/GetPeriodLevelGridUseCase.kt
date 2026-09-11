package com.emm.gema.core.domain.evaluation

import com.emm.gema.core.domain.curriculum.Competency
import com.emm.gema.core.domain.curriculum.GetPeriodCompetenciesUseCase
import com.emm.gema.core.domain.curriculum.PeriodCompetency
import com.emm.gema.core.domain.schoolyear.PeriodId
import com.emm.gema.core.domain.section.Area
import com.emm.gema.core.domain.section.SectionId
import com.emm.gema.core.domain.student.Student
import com.emm.gema.core.domain.student.StudentRepository
import com.emm.gema.core.domain.student.orderedByName
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetPeriodLevelGridUseCase(
    private val getPeriodCompetencies: GetPeriodCompetenciesUseCase,
    private val studentRepository: StudentRepository,
    private val periodLevelRepository: PeriodLevelRepository,
) {

    operator fun invoke(sectionId: SectionId, periodId: PeriodId, area: Area): Flow<PeriodLevelGrid> = combine(
        getPeriodCompetencies(sectionId = sectionId, periodId = periodId, area = area),
        studentRepository.observeBySection(sectionId),
        periodLevelRepository.observeByPeriod(sectionId = sectionId, periodId = periodId),
    ) { competencies: List<PeriodCompetency>, students: List<Student>, levels: List<PeriodLevel> ->
        val columns: List<Competency> = competencies.filter { it.isWorked }.map { it.competency }
        val recorded: Map<PeriodLevelKey, PeriodLevel> = levels.associateBy { it.key }

        PeriodLevelGrid(
            columns = columns,
            rows = students.filter { !it.isWithdrawn }.orderedByName().map { student ->
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
        )
    }
}
