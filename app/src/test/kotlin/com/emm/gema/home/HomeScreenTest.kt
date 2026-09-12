package com.emm.gema.home

import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.emm.gema.core.domain.section.SectionId
import com.emm.gema.core.theme.GemaTheme
import com.emm.gema.core.ui.test.RobolectricComposeTest
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class HomeScreenTest : RobolectricComposeTest() {

    @Test
    fun `tapping a section card dispatches SectionClicked`() {
        var dispatched: HomeUiIntent? = null
        val sectionId = SectionId("section-1")
        val state = HomeUiState(
            isLoading = false,
            sections = listOf(SectionRow(sectionId, "3ro A", 30)),
        )

        composeTestRule.setContent {
            GemaTheme {
                HomeScreen(state = state, onIntent = { dispatched = it })
            }
        }

        composeTestRule.onNodeWithTag(sectionCardTestTag(sectionId)).performClick()

        assertThat(dispatched).isEqualTo(HomeUiIntent.SectionClicked(sectionId))
    }
}
