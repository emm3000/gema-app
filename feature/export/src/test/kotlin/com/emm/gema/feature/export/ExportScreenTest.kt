package com.emm.gema.feature.export

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.emm.gema.core.domain.schoolyear.PeriodId
import com.emm.gema.core.theme.GemaTheme
import java.time.YearMonth
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExportScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

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

        composeTestRule.onNodeWithText("Exportar").assertIsNotEnabled()
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
        composeTestRule.onNodeWithText("Exportar").assertIsEnabled()
        composeTestRule.onNodeWithText("PDF").assertIsEnabled()
        composeTestRule.onNodeWithText("CSV").assertIsEnabled()
    }
}
