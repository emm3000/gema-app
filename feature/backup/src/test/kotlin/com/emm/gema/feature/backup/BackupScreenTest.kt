package com.emm.gema.feature.backup

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.emm.gema.core.theme.GemaTheme
import com.emm.gema.core.ui.test.RobolectricComposeTest
import com.google.common.truth.Truth.assertThat
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
    fun `restore confirm dialog announces the restart is not an error`() {
        val confirmation = RestoreConfirmation(
            uri = "content://backup.gema",
            fileName = "backup.gema",
            currentSchoolYearCount = 1,
            currentStudentCount = 57,
        )

        composeTestRule.setContent {
            GemaTheme {
                BackupScreen(state = baseState.copy(restoreConfirmation = confirmation), onIntent = {})
            }
        }

        composeTestRule.onNodeWithText("La app se reinicia al terminar. No es un error.").assertExists()
        composeTestRule.onNodeWithText("57", substring = true).assertExists()
    }

    @Test
    fun `overdue last backup strip announces the overdue state`() {
        composeTestRule.setContent {
            GemaTheme {
                BackupScreen(
                    state = baseState.copy(daysSinceLastBackup = 12, isBackupOverdue = true),
                    onIntent = {},
                )
            }
        }

        composeTestRule.onNodeWithText("ÚLTIMO RESPALDO", substring = true)
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Respaldo atrasado"))
    }

    @Test
    fun `last backup strip announces no overdue state while the reminder is not due`() {
        composeTestRule.setContent {
            GemaTheme {
                BackupScreen(
                    state = baseState.copy(daysSinceLastBackup = 2, isBackupOverdue = false),
                    onIntent = {},
                )
            }
        }

        composeTestRule.onNodeWithText("ÚLTIMO RESPALDO", substring = true)
            .assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.StateDescription))
    }

    @Test
    fun `back button sends BackClicked`() {
        val intents: MutableList<BackupUiIntent> = mutableListOf()
        composeTestRule.setContent {
            GemaTheme {
                BackupScreen(state = baseState, onIntent = { intents += it })
            }
        }

        composeTestRule.onNodeWithContentDescription("Volver").performClick()

        assertThat(intents).containsExactly(BackupUiIntent.BackClicked)
    }
}
