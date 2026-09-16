package com.emm.gema.feature.setup.periods

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import com.emm.gema.core.domain.schoolyear.PeriodId
import com.emm.gema.core.domain.schoolyear.PeriodKind
import com.emm.gema.core.theme.GemaTheme
import com.emm.gema.core.ui.test.RobolectricComposeTest
import com.google.common.truth.Truth.assertThat
import java.time.LocalDate
import org.junit.Test

class PeriodsScreenTest : RobolectricComposeTest() {

    private val firstPeriod: PeriodRow = PeriodRow(
        id = PeriodId("period-1"),
        number = 1,
        label = "I Bimestre",
        startDate = LocalDate.of(2026, 3, 2),
        endDate = LocalDate.of(2026, 5, 13),
        isCurrent = false,
    )
    private val secondPeriod: PeriodRow = PeriodRow(
        id = PeriodId("period-2"),
        number = 2,
        label = "II Bimestre",
        startDate = LocalDate.of(2026, 5, 18),
        endDate = LocalDate.of(2026, 8, 14),
        isCurrent = true,
    )
    private val baseState: PeriodsUiState = PeriodsUiState(
        isLoading = false,
        schoolYearLabel = "2026",
        periodKind = PeriodKind.BIMESTER,
        periods = listOf(firstPeriod, secondPeriod),
        canSave = true,
    )

    @Test
    fun `first period start field is named by its period label`() {
        composeTestRule.setContent {
            GemaTheme {
                PeriodsScreen(state = baseState, onIntent = {})
            }
        }

        composeTestRule.onNodeWithContentDescription("Inicio del I Bimestre", substring = true).assertIsDisplayed()
    }

    @Test
    fun `first period end field is named by its period label`() {
        composeTestRule.setContent {
            GemaTheme {
                PeriodsScreen(state = baseState, onIntent = {})
            }
        }

        composeTestRule.onNodeWithContentDescription("Fin del I Bimestre", substring = true).assertIsDisplayed()
    }

    @Test
    fun `overlap error is still announced on the overlapping field`() {
        val overlappingState: PeriodsUiState = baseState.copy(
            overlappingStartIds = setOf(secondPeriod.id),
        )
        composeTestRule.setContent {
            GemaTheme {
                PeriodsScreen(state = overlappingState, onIntent = {})
            }
        }

        val errorProperty: String? = composeTestRule
            .onNodeWithContentDescription("Inicio del II Bimestre", substring = true)
            .fetchSemanticsNode()
            .config
            .getOrNull(SemanticsProperties.Error)

        assertThat(errorProperty).isNotNull()
    }
}
