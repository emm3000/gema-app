package com.emm.gema.core.ui

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.emm.gema.core.theme.GemaSpacing
import com.emm.gema.core.theme.GemaTheme

private val iconGlyphSize: Dp = 22.dp

@Composable
fun GIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.sizeIn(
            minWidth = GemaSpacing.minimumTouchTarget,
            minHeight = GemaSpacing.minimumTouchTarget,
        ),
        enabled = isEnabled,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(iconGlyphSize),
            tint = if (isEnabled) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.outline
            },
        )
    }
}

@PreviewLightDark
@Composable
private fun GIconButtonPreview() {
    GemaTheme {
        GIconButton(
            icon = Icons.Filled.Delete,
            contentDescription = "Eliminar",
            onClick = {},
        )
    }
}
