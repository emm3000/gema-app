package com.emm.gema.core.ui.test

import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
abstract class RobolectricComposeTest {

    @get:Rule
    val composeTestRule: ComposeContentTestRule = createComposeRule()
}
