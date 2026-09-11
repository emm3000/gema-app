package com.emm.gema.core.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.emm.gema.core.theme.GemaShapes

@Composable
fun GDialog(
    title: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    dismissText: String? = null,
    isDestructive: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        shape = GemaShapes.container,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text(text = title, style = MaterialTheme.typography.titleMedium) },
        text = { Column(content = content) },
        confirmButton = {
            GButton(
                text = confirmText,
                onClick = onConfirm,
                variant = if (isDestructive) GButtonVariant.DESTRUCTIVE else GButtonVariant.PRIMARY,
            )
        },
        dismissButton = {
            if (dismissText != null) {
                GButton(text = dismissText, onClick = onDismiss, variant = GButtonVariant.TEXT)
            }
        },
    )
}
