package com.emm.gema.feature.export

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
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
    fun `resumen pdf button sends ExportSummaryPdfClicked`() {
        val intents: MutableList<ExportUiIntent> = mutableListOf()
        composeTestRule.setContent {
            GemaTheme {
                ExportScreen(state = baseState, onIntent = { intents += it })
            }
        }

        composeTestRule.onNodeWithContentDescription("Generar resumen en PDF").performScrollTo().performClick()

        assertThat(intents).containsExactly(ExportUiIntent.ExportSummaryPdfClicked)
    }

    @Test
    fun `resumen csv button sends ExportSummaryCsvClicked`() {
        val intents: MutableList<ExportUiIntent> = mutableListOf()
        composeTestRule.setContent {
            GemaTheme {
                ExportScreen(state = baseState, onIntent = { intents += it })
            }
        }

        composeTestRule.onNodeWithContentDescription("Generar resumen en CSV").performScrollTo().performClick()

        assertThat(intents).containsExactly(ExportUiIntent.ExportSummaryCsvClicked)
    }

    @Test
    fun `resumen pdf button keeps its description while busy`() {
        composeTestRule.setContent {
            GemaTheme {
                ExportScreen(state = baseState.copy(activeExport = ActiveExport.SUMMARY_PDF), onIntent = {})
            }
        }

        composeTestRule.onNodeWithContentDescription("Generar resumen en PDF").performScrollTo().assertIsNotEnabled()
    }

    @Test
    fun `resumen pdf button is primary only when grades export is unavailable`() {
        assertThat(summaryPdfVariant(GradesExportUiState.Unavailable)).isEqualTo(GButtonVariant.PRIMARY)
        assertThat(summaryPdfVariant(GradesExportUiState.Ready)).isEqualTo(GButtonVariant.SECONDARY)
        assertThat(summaryPdfVariant(GradesExportUiState.Blocked(emptyList()))).isEqualTo(GButtonVariant.SECONDARY)
    }

    @Test
    fun `import template link sends ImportTemplateClicked`() {
        val intents: MutableList<ExportUiIntent> = mutableListOf()
        composeTestRule.setContent {
            GemaTheme {
                ExportScreen(
                    state = baseState.copy(gradesExportState = GradesExportUiState.Unavailable),
                    onIntent = { intents += it },
                )
            }
        }

        composeTestRule.onNodeWithText("Importar plantilla").performClick()

        assertThat(intents).containsExactly(ExportUiIntent.ImportTemplateClicked)
    }

    @Test
    fun `back button sends BackClicked`() {
        val intents: MutableList<ExportUiIntent> = mutableListOf()
        composeTestRule.setContent {
            GemaTheme {
                ExportScreen(state = baseState, onIntent = { intents += it })
            }
        }

        composeTestRule.onNodeWithContentDescription("Volver").performClick()

        assertThat(intents).containsExactly(ExportUiIntent.BackClicked)
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

    @Test
    fun `blocked state renders the singular gap count`() {
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

        composeTestRule.onNodeWithText("1 conclusión descriptiva falta").assertIsEnabled()
    }

    @Test
    fun `blocked state renders the plural gap count`() {
        val gaps: List<ExportGapRow> = listOf(
            ExportGapRow(
                studentId = StudentId("student-1"),
                studentName = "BAUTISTA QUISPE, JOSE",
                competencyId = CompetencyId("PPSS-2"),
                competencyLabel = "Personal Social · 02",
            ),
            ExportGapRow(
                studentId = StudentId("student-2"),
                studentName = "CCOYLLO MAMANI, ANA",
                competencyId = CompetencyId("PPSS-3"),
                competencyLabel = "Personal Social · 03",
            ),
        )
        composeTestRule.setContent {
            GemaTheme {
                ExportScreen(
                    state = baseState.copy(gradesExportState = GradesExportUiState.Blocked(gaps)),
                    onIntent = {},
                )
            }
        }

        composeTestRule.onNodeWithText("2 conclusiones descriptivas faltan").assertIsEnabled()
    }
}
