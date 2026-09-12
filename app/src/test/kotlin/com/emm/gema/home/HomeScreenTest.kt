package com.emm.gema.home

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.emm.gema.core.domain.section.SectionId
import com.emm.gema.core.theme.GemaTheme
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `tapping a section card dispatches SectionClicked`() {
        var dispatched: HomeUiIntent? = null
        val state = HomeUiState(
            isLoading = false,
            sections = listOf(SectionRow(SectionId("section-1"), "3ro A", 30)),
        )

        composeTestRule.setContent {
            GemaTheme {
                HomeScreen(state = state, onIntent = { dispatched = it })
            }
        }

        composeTestRule.onNodeWithText("3ro A").performClick()

        assertThat(dispatched).isEqualTo(HomeUiIntent.SectionClicked(SectionId("section-1")))
    }
}
