package com.emm.gema.feature.backup

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import com.emm.gema.core.theme.GemaTheme
import com.emm.gema.core.ui.test.RobolectricComposeTest
import org.junit.Test

class BackupScreenTest : RobolectricComposeTest() {

    private val baseState: BackupUiState = BackupUiState(isLoading = false)

    @Test
    fun `restore banner states the loss cannot be undone`() {
        composeTestRule.setContent {
            GemaTheme {
                BackupScreen(state = baseState.copy(restoreConfirmation = null), onIntent = {})
            }
        }

        composeTestRule.onNodeWithText("No se puede deshacer.", substring = true).assertExists()
    }

    @Test
    fun `restore confirm dialog states the loss cannot be undone`() {
        val confirmation = RestoreConfirmation(
            uri = "content://backup.gema",
            fileName = "backup.gema",
            currentSchoolYearCount = 1,
        )

        composeTestRule.setContent {
            GemaTheme {
                BackupScreen(state = baseState.copy(restoreConfirmation = confirmation), onIntent = {})
            }
        }

        composeTestRule.onAllNodesWithText("No se puede deshacer.", substring = true).assertCountEquals(2)
    }
}
