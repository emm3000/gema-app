package com.emm.gema.core.ui

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.dp
import com.emm.gema.core.theme.GemaTheme
import com.emm.gema.core.ui.test.RobolectricComposeTest
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class GBannerTest : RobolectricComposeTest() {

    @Test
    fun `side link action is a 48dp button that sends one click`() {
        var clicks: Int = 0
        setLinkBanner(onActionClick = { clicks++ })

        composeTestRule.onNode(hasText(LINK_TEXT) and hasClickAction())
            .assertHeightIsAtLeast(48.dp)
            .assertWidthIsAtLeast(48.dp)
            .assertHasClickAction()
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
            .performClick()

        assertThat(clicks).isEqualTo(1)
    }

    @Test
    fun `side link action does not shift the banner layout`() {
        setLinkBanner(onActionClick = {})

        val bannerBounds: DpRect = composeTestRule.onNodeWithTag(BANNER_TAG).getUnclippedBoundsInRoot()
        val messageBounds: DpRect = composeTestRule.onNodeWithText(MESSAGE_TEXT).getUnclippedBoundsInRoot()

        val linkTextBounds: DpRect = composeTestRule.onNodeWithText(LINK_TEXT, useUnmergedTree = true)
            .getUnclippedBoundsInRoot()

        assertThat(bannerBounds).isEqualTo(DpRect(0.dp, 0.dp, 320.dp, 68.dp))
        assertThat(messageBounds).isEqualTo(DpRect(32.dp, 16.dp, 44.dp, 52.dp))
        assertThat(linkTextBounds).isEqualTo(DpRect(288.dp, 16.dp, 304.dp, 52.dp))
    }

    private fun setLinkBanner(onActionClick: () -> Unit) {
        composeTestRule.setContent {
            GemaTheme {
                GBanner(
                    text = MESSAGE_TEXT,
                    modifier = Modifier.testTag(BANNER_TAG),
                    tone = GBannerTone.ERROR,
                    hasLeadingDot = true,
                    actionText = LINK_TEXT,
                    onActionClick = onActionClick,
                    actionStyle = GBannerActionStyle.LINK,
                )
            }
        }
    }

    private companion object {
        const val BANNER_TAG: String = "banner"
        const val MESSAGE_TEXT: String = "Sin respaldo"
        const val LINK_TEXT: String = "Respaldar ahora"
    }
}
