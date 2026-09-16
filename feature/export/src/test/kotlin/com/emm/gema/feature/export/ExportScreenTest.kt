package com.emm.gema.feature.export

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithText
import com.emm.gema.core.domain.curriculum.CompetencyId
import com.emm.gema.core.domain.schoolyear.PeriodId
import com.emm.gema.core.domain.student.StudentId
import com.emm.gema.core.theme.GemaTheme
import com.emm.gema.core.ui.GButtonVariant
import com.emm.gema.core.ui.test.RobolectricComposeTest
import com.google.common.truth.Truth.assertThat
import java.time.YearMonth
import org.junit.Test

class ExportScreenTest : RobolectricComposeTest() {

    private val baseState: ExportUiState = ExportUiState(
        isLoading = false,
        sectionTitle = "6to A",
        periodId = PeriodId("period-1"),
        periodLabel = "II Bimestre",
        templateFileName = "6 Primaria EBR.xlsx",
        attendanceMonth = YearMonth.of(2026, 9),
        attendanceDayCount = 20,
        gradesExportState = GradesExportUiState.Ready,
    )

    @Test
    fun `sibling actions disable while grades export runs`() {
        composeTestRule.setContent {
            GemaTheme {
                ExportScreen(state = baseState.copy(activeExport = ActiveExport.GRADES), onIntent = {})
            }
        }

        composeTestRule.onNodeWithText("Exportar el mes").assertIsNotEnabled()
        composeTestRule.onNodeWithText("PDF").assertIsNotEnabled()
        composeTestRule.onNodeWithText("CSV").assertIsNotEnabled()
    }

    @Test
    fun `sibling actions disable while attendance export runs`() {
        composeTestRule.setContent {
            GemaTheme {
                ExportScreen(state = baseState.copy(activeExport = ActiveExport.ATTENDANCE), onIntent = {})
            }
        }

        composeTestRule.onNodeWithText("Generar archivo").assertIsNotEnabled()
        composeTestRule.onNodeWithText("PDF").assertIsNotEnabled()
        composeTestRule.onNodeWithText("CSV").assertIsNotEnabled()
    }

    @Test
    fun `every action stays enabled when nothing is exporting`() {
        composeTestRule.setContent {
            GemaTheme {
                ExportScreen(state = baseState.copy(activeExport = null), onIntent = {})
            }
        }

        composeTestRule.onNodeWithText("Generar archivo").assertIsEnabled()
        composeTestRule.onNodeWithText("Exportar el mes").assertIsEnabled()
        composeTestRule.onNodeWithText("PDF").assertIsEnabled()
        composeTestRule.onNodeWithText("CSV").assertIsEnabled()
    }

    @Test
    fun `resumen pdf button is primary only when grades export is unavailable`() {
        assertThat(summaryPdfVariant(GradesExportUiState.Unavailable)).isEqualTo(GButtonVariant.PRIMARY)
        assertThat(summaryPdfVariant(GradesExportUiState.Ready)).isEqualTo(GButtonVariant.SECONDARY)
        assertThat(summaryPdfVariant(GradesExportUiState.Blocked(emptyList()))).isEqualTo(GButtonVariant.SECONDARY)
    }

    @Test
    fun `unavailable state renders the import template banner action`() {
        composeTestRule.setContent {
            GemaTheme {
                ExportScreen(state = baseState.copy(gradesExportState = GradesExportUiState.Unavailable), onIntent = {})
            }
        }

        composeTestRule.onNodeWithText("Importar plantilla").assertIsEnabled()
    }

    @Test
    fun `blocked state renders the gap row`() {
        val gap = ExportGapRow(
            studentId = StudentId("student-1"),
            studentName = "BAUTISTA QUISPE, JOSE",
            competencyId = CompetencyId("PPSS-2"),
            competencyLabel = "Personal Social · 02",
        )
        composeTestRule.setContent {
            GemaTheme {
                ExportScreen(
                    state = baseState.copy(gradesExportState = GradesExportUiState.Blocked(listOf(gap))),
                    onIntent = {},
                )
            }
        }

        composeTestRule.onNodeWithText("BAUTISTA QUISPE, JOSE").assertIsEnabled()
        composeTestRule.onNodeWithText("Generar archivo").assertIsNotEnabled()
    }
}
