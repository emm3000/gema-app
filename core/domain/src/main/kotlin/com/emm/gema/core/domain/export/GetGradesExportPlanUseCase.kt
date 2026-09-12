package com.emm.gema.core.domain.export

import com.emm.gema.core.domain.curriculum.CompetencyId
import com.emm.gema.core.domain.evaluation.GetPeriodLevelGridUseCase
import com.emm.gema.core.domain.evaluation.PeriodLevel
import com.emm.gema.core.domain.evaluation.PeriodLevelGrid
import com.emm.gema.core.domain.evaluation.PeriodLevelGridRow
import com.emm.gema.core.domain.schoolyear.PeriodId
import com.emm.gema.core.domain.section.Area
import com.emm.gema.core.domain.section.GetSectionAreasUseCase
import com.emm.gema.core.domain.section.SectionArea
import com.emm.gema.core.domain.section.SectionId
import com.emm.gema.core.domain.siagie.SiagieGradeEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class GetGradesExportPlanUseCase(
    private val getSectionAreas: GetSectionAreasUseCase,
    private val getPeriodLevelGrid: GetPeriodLevelGridUseCase,
) {

    operator fun invoke(sectionId: SectionId, periodId: PeriodId): Flow<GradesExportPlan> = getSectionAreas(sectionId)
        .map { areas: List<SectionArea> -> areas.filter { it.isActive }.map { it.area } }
        .flatMapLatest { active: List<Area> -> planOf(sectionId, periodId, active) }

    private fun planOf(sectionId: SectionId, periodId: PeriodId, areas: List<Area>): Flow<GradesExportPlan> {
        if (areas.isEmpty()) return flowOf(GradesExportPlan())
        val grids: List<Flow<AreaGrid>> = areas.map { area: Area ->
            getPeriodLevelGrid(sectionId = sectionId, periodId = periodId, area = area)
                .map { grid: PeriodLevelGrid -> AreaGrid(area, grid) }
        }
        return combine(grids) { filled: Array<AreaGrid> -> planOf(filled.toList()) }
    }

    private fun planOf(grids: List<AreaGrid>): GradesExportPlan = GradesExportPlan(
        gaps = grids.flatMap { it.gaps() },
        entries = grids.flatMap { it.entries() },
    )
}

private class AreaGrid(val area: Area, val grid: PeriodLevelGrid) {

    fun gaps(): List<ExportGap> = grid.rows.flatMap { row: PeriodLevelGridRow ->
        row.cells.filter { it.isIncomplete }.map { cell: PeriodLevel ->
            ExportGap(
                studentId = row.student.id,
                studentName = row.student.fullName,
                competency = columnOf(cell.key.competencyId),
            )
        }
    }

    fun entries(): List<SiagieGradeEntry> = grid.rows.flatMap { row: PeriodLevelGridRow ->
        row.cells.filter { it.isRecorded }.map { cell: PeriodLevel ->
            SiagieGradeEntry(
                area = area,
                siagieOrdinal = columnOf(cell.key.competencyId).siagieOrdinal,
                studentCode = row.student.code,
                achievementValue = achievementValueOf(cell),
                descriptiveConclusion = cell.descriptiveConclusion,
            )
        }
    }

    private fun achievementValueOf(cell: PeriodLevel): String =
        cell.achievementLevel?.name ?: requireNotNull(cell.unworkedComment).siagieValue

    private fun columnOf(competencyId: CompetencyId) = grid.columns.first { it.id == competencyId }
}
