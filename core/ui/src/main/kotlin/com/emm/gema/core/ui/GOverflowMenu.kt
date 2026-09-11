@file:Suppress("MatchingDeclarationName")

package com.emm.gema.core.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.emm.gema.core.theme.GemaTheme

data class GMenuAction(
    val label: String,
    val onClick: () -> Unit,
)

@Composable
fun GOverflowMenu(
    actions: List<GMenuAction>,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    var isExpanded: Boolean by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        GIconButton(
            icon = Icons.Filled.MoreVert,
            contentDescription = contentDescription,
            onClick = { isExpanded = true },
        )
        DropdownMenu(expanded = isExpanded, onDismissRequest = { isExpanded = false }) {
            actions.forEach { action ->
                DropdownMenuItem(
                    text = { Text(text = action.label) },
                    onClick = {
                        isExpanded = false
                        action.onClick()
                    },
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun GOverflowMenuPreview() {
    GemaTheme {
        GOverflowMenu(
            actions = listOf(
                GMenuAction(label = "Renombrar", onClick = {}),
                GMenuAction(label = "Áreas", onClick = {}),
            ),
            contentDescription = "Más opciones",
        )
    }
}
