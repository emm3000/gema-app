package com.emm.gema.home

import android.app.Application
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.emm.gema.core.domain.attendance.AttendanceDaySummary
import com.emm.gema.core.domain.section.SectionId
import com.emm.gema.core.theme.GemaTheme
import com.emm.gema.core.ui.test.RobolectricComposeTest
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.robolectric.annotation.Config

@Config(application = Application::class)
class HomeScreenTest : RobolectricComposeTest() {

    private val takenId: SectionId = SectionId("section-taken")
    private val firstPendingId: SectionId = SectionId("section-pending-1")
    private val secondPendingId: SectionId = SectionId("section-pending-2")
    private val state: HomeUiState = HomeUiState(
        isLoading = false,
        todayLabel = "HOY · MARTES 10 DE SETIEMBRE",
        sections = listOf(
            SectionRow(takenId, "4to B", 27, AttendanceDaySummary(25, 27, 0), missingLevelCount = 12),
            SectionRow(firstPendingId, "3ro A", 30, missingLevelCount = 12),
            SectionRow(secondPendingId, "5to C", 20),
        ),
    )

    private var dispatched: HomeUiIntent? = null

    private fun render(homeState: HomeUiState = state) {
        composeTestRule.setContent {
            GemaTheme {
                HomeScreen(state = homeState, onIntent = { dispatched = it })
            }
        }
    }

    @Test
    fun `only the first pending section carries the primary action`() {
        render()

        composeTestRule.onAllNodesWithTag(HOME_PRIMARY_ACTION_TEST_TAG).assertCountEquals(1)
        composeTestRule.onNodeWithTag(HOME_PRIMARY_ACTION_TEST_TAG).performClick()

        assertThat(dispatched).isEqualTo(HomeUiIntent.TakeAttendanceClicked(firstPendingId))
    }

    @Test
    fun `a second pending section dispatches its own attendance intent from a secondary action`() {
        render()

        composeTestRule.onNodeWithTag(sectionAttendanceButtonTestTag(secondPendingId)).performClick()

        assertThat(dispatched).isEqualTo(HomeUiIntent.TakeAttendanceClicked(secondPendingId))
    }

    @Test
    fun `a taken section has no attendance action`() {
        render()

        composeTestRule.onNodeWithTag(sectionAttendanceButtonTestTag(takenId)).assertDoesNotExist()
    }

    @Test
    fun `tapping a taken section row dispatches SectionClicked`() {
        render()

        composeTestRule.onNodeWithTag(sectionRowTestTag(takenId)).performClick()

        assertThat(dispatched).isEqualTo(HomeUiIntent.SectionClicked(takenId))
    }

    @Test
    fun `tapping a pending section row dispatches SectionClicked`() {
        render()

        composeTestRule.onNodeWithTag(sectionRowTestTag(firstPendingId)).performClick()

        assertThat(dispatched).isEqualTo(HomeUiIntent.SectionClicked(firstPendingId))
    }

    @Test
    fun `there is no primary action once every section is taken`() {
        render(
            HomeUiState(
                isLoading = false,
                sections = listOf(SectionRow(takenId, "4to B", 27, AttendanceDaySummary(27, 27, 0))),
            ),
        )

        composeTestRule.onAllNodesWithTag(HOME_PRIMARY_ACTION_TEST_TAG).assertCountEquals(0)
    }
}
