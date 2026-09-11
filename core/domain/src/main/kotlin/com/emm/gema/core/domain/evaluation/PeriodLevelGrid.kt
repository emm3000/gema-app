package com.emm.gema.core.domain.evaluation

import com.emm.gema.core.domain.curriculum.Competency
import com.emm.gema.core.domain.student.Student

data class PeriodLevelGrid(
    val columns: List<Competency>,
    val rows: List<PeriodLevelGridRow>,
) {
    val missingCount: Int get() = rows.sumOf { row -> row.cells.count { !it.isRecorded } }

    val incompleteCount: Int get() = rows.sumOf { row -> row.cells.count { it.isIncomplete } }
}

data class PeriodLevelGridRow(
    val student: Student,
    val cells: List<PeriodLevel>,
)
