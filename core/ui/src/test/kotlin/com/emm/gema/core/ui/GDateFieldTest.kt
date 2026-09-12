package com.emm.gema.core.ui

import androidx.compose.foundation.layout.width
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.emm.gema.core.theme.GemaTheme
import com.emm.gema.core.ui.test.RobolectricComposeTest
import com.google.common.truth.Truth.assertThat
import java.time.LocalDate
import org.junit.Test
import org.robolectric.annotation.GraphicsMode

@GraphicsMode(GraphicsMode.Mode.NATIVE)
class GDateFieldTest : RobolectricComposeTest() {

    @Test
    fun `field grows past a single-line baseline when its value does not fit the container width`() {
        val value: LocalDate = LocalDate.of(2026, 3, 1)
        val width: Dp = 130.dp

        composeTestRule.setContent {
            GemaTheme {
                OutlinedTextField(
                    value = "01/03/2026",
                    onValueChange = {},
                    singleLine = true,
                    modifier = Modifier.width(width).testTag("baseline"),
                )
                GDateField(
                    value = value,
                    onValueChange = {},
                    modifier = Modifier.width(width).testTag("field"),
                )
            }
        }

        val baselineHeightPx: Int = composeTestRule.onNodeWithTag("baseline")
            .fetchSemanticsNode()
            .size
            .height
        val fieldHeightPx: Int = composeTestRule.onNodeWithTag("field")
            .fetchSemanticsNode()
            .size
            .height

        assertThat(fieldHeightPx).isGreaterThan(baselineHeightPx)
    }
}
