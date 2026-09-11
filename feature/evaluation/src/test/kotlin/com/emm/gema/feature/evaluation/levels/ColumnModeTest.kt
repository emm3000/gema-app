package com.emm.gema.feature.evaluation.levels

import com.emm.gema.core.domain.evaluation.AchievementLevel
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ColumnModeTest {

    @Test
    fun `entering binds the mode to the first visible student`() {
        val state: PeriodLevelsUiState = grid.enteringColumnMode("PPSS-1")

        assertThat(state.columnMode?.competencyId).isEqualTo("PPSS-1")
        assertThat(state.columnModeStudent?.studentId).isEqualTo("student-1")
    }

    @Test
    fun `entering an unknown column changes nothing`() {
        assertThat(grid.enteringColumnMode("MATE-1").columnMode).isNull()
    }

    @Test
    fun `entering with no visible student changes nothing`() {
        assertThat(grid.copy(rows = emptyList()).enteringColumnMode("PPSS-1").columnMode).isNull()
    }

    @Test
    fun `entering closes an open sheet`() {
        val open: PeriodLevelsUiState = grid.copy(
            sheet = PeriodLevelSheetUiState(
                studentId = "student-1",
                competencyId = "PPSS-1",
                studentName = "ACOSTA RIVERA, Luz",
                competencyLabel = "01 Construye su identidad",
            ),
        )

        assertThat(open.enteringColumnMode("PPSS-1").sheet).isNull()
    }

    @Test
    fun `advancing walks the visible rows in order`() {
        val state: PeriodLevelsUiState = grid.enteringColumnMode("PPSS-1").advancedToNextStudent()

        assertThat(state.columnMode?.currentStudentIndex).isEqualTo(1)
        assertThat(state.columnModeStudent?.studentId).isEqualTo("student-2")
    }

    @Test
    fun `advancing past the last student leaves the mode`() {
        val state: PeriodLevelsUiState = grid
            .enteringColumnMode("PPSS-1")
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
            .enteringColumnMode("PPSS-1")

        assertThat(filtered.columnModeStudent?.studentId).isEqualTo("student-2")
        assertThat(filtered.advancedToNextStudent().columnMode).isNull()
    }

    private val grid: PeriodLevelsUiState = PeriodLevelsUiState(
        isLoading = false,
        columns = listOf(CompetencyColumn("PPSS-1", 1, "Construye su identidad")),
        rows = listOf(
            PeriodLevelRow(
                studentId = "student-1",
                displayName = "ACOSTA RIVERA, Luz",
                cells = listOf(
                    PeriodLevelCell(
                        competencyId = "PPSS-1",
                        achievementLevel = AchievementLevel.A,
                        unworkedComment = null,
                        hasDescriptiveConclusion = false,
                        isIncomplete = false,
                    ),
                ),
            ),
            PeriodLevelRow(
                studentId = "student-2",
                displayName = "BAUTISTA QUISPE, Jose",
                cells = listOf(
                    PeriodLevelCell(
                        competencyId = "PPSS-1",
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
