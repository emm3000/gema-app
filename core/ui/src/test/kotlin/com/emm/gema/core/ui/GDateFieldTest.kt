package com.emm.gema.core.ui

import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import com.emm.gema.core.theme.GemaTheme
import java.time.LocalDate
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

// Covers the field growing past one line to keep a value visible; it cannot
// assert against ellipsis or horizontal-scroll clipping, since neither is
// observable through Compose semantics at the JVM level (see #155).
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class GDateFieldTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `field grows past single-line height when its value does not fit the container width`() {
        val value: LocalDate = LocalDate.of(2026, 3, 1)

        composeTestRule.setContent {
            GemaTheme {
                GDateField(
                    value = value,
                    onValueChange = {},
                    modifier = Modifier.width(130.dp),
                )
            }
        }

        composeTestRule.onNodeWithText("01/03/2026")
            .assertHeightIsAtLeast(64.dp)
    }
}
