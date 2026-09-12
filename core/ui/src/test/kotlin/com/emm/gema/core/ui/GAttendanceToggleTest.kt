package com.emm.gema.core.ui

import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.onNodeWithContentDescription
import com.emm.gema.core.theme.GemaTheme
import com.emm.gema.core.ui.test.RobolectricComposeTest
import org.junit.Test

class GAttendanceToggleTest : RobolectricComposeTest() {

    @Test
    fun `each option announces its label and selected state`() {
        composeTestRule.setContent {
            GemaTheme {
                GAttendanceToggle(option = GAttendanceOption.PRESENT, isRecorded = true, onSelect = {})
            }
        }

        composeTestRule.onNodeWithContentDescription(GAttendanceOption.PRESENT.contentDescription)
            .assertIsSelected()
        composeTestRule.onNodeWithContentDescription(GAttendanceOption.LATE.contentDescription)
            .assertIsNotSelected()
        composeTestRule.onNodeWithContentDescription(GAttendanceOption.ABSENT.contentDescription)
            .assertIsNotSelected()
        composeTestRule.onNodeWithContentDescription(GAttendanceOption.JUSTIFIED.contentDescription)
            .assertIsNotSelected()
    }
}
