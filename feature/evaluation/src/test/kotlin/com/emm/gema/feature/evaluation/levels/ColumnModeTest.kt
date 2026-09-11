package com.emm.gema.feature.evaluation.levels

import com.emm.gema.core.domain.curriculum.CompetencyId
import com.emm.gema.core.domain.evaluation.AchievementLevel
import com.emm.gema.core.domain.student.StudentId
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ColumnModeTest {

    @Test
    fun `entering binds the mode to the first visible student`() {
        val state: PeriodLevelsUiState = grid.enteringColumnMode(firstPpssId)

        assertThat(state.columnMode?.competencyId).isEqualTo(firstPpssId)
        assertThat(state.columnModeStudent?.studentId).isEqualTo(firstStudentId)
    }

    @Test
    fun `entering an unknown column changes nothing`() {
        assertThat(grid.enteringColumnMode(firstMateId).columnMode).isNull()
    }

    @Test
    fun `entering with no visible student changes nothing`() {
        assertThat(grid.copy(rows = emptyList()).enteringColumnMode(firstPpssId).columnMode).isNull()
    }

    @Test
    fun `entering closes an open sheet`() {
        val open: PeriodLevelsUiState = grid.copy(
            sheet = PeriodLevelSheetUiState(
                studentId = firstStudentId,
                competencyId = firstPpssId,
                studentName = "ACOSTA RIVERA, Luz",
                competencyLabel = "01 Construye su identidad",
            ),
        )

        assertThat(open.enteringColumnMode(firstPpssId).sheet).isNull()
    }

    @Test
    fun `advancing walks the visible rows in order`() {
        val state: PeriodLevelsUiState = grid.enteringColumnMode(firstPpssId).advancedToNextStudent()

        assertThat(state.columnMode?.currentStudentIndex).isEqualTo(1)
        assertThat(state.columnModeStudent?.studentId).isEqualTo(secondStudentId)
    }

    @Test
    fun `advancing past the last student leaves the mode`() {
        val state: PeriodLevelsUiState = grid
            .enteringColumnMode(firstPpssId)
            .advancedToNextStudent()
            .advancedToNextStudent()

        assertThat(state.columnMode).isNull()
    }

    @Test
    fun `advancing outside the mode changes nothing`() {
        assertThat(grid.advancedToNextStudent()).isEqualTo(grid)
    }

    @Test
    fun `the missing filter narrows what the mode walks`() {
        val filtered: PeriodLevelsUiState = grid
            .copy(isMissingFilterOn = true)
            .enteringColumnMode(firstPpssId)

        assertThat(filtered.columnModeStudent?.studentId).isEqualTo(secondStudentId)
        assertThat(filtered.advancedToNextStudent().columnMode).isNull()
    }

    private val grid: PeriodLevelsUiState = PeriodLevelsUiState(
        isLoading = false,
        columns = listOf(CompetencyColumn(firstPpssId, 1, "Construye su identidad")),
        rows = listOf(
            PeriodLevelRow(
                studentId = firstStudentId,
                displayName = "ACOSTA RIVERA, Luz",
                cells = listOf(
                    PeriodLevelCell(
                        competencyId = firstPpssId,
                        achievementLevel = AchievementLevel.A,
                        unworkedComment = null,
                        hasDescriptiveConclusion = false,
                        isIncomplete = false,
                    ),
                ),
            ),
            PeriodLevelRow(
                studentId = secondStudentId,
                displayName = "BAUTISTA QUISPE, Jose",
                cells = listOf(
                    PeriodLevelCell(
                        competencyId = firstPpssId,
                        achievementLevel = null,
                        unworkedComment = null,
                        hasDescriptiveConclusion = false,
                        isIncomplete = false,
                    ),
                ),
            ),
        ),
    )
}

private val firstStudentId: StudentId = StudentId("student-1")

private val secondStudentId: StudentId = StudentId("student-2")

private val firstPpssId: CompetencyId = CompetencyId("PPSS-1")

private val firstMateId: CompetencyId = CompetencyId("MATE-1")
