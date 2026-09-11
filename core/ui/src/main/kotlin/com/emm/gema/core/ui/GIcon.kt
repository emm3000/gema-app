package com.emm.gema.core.ui

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.emm.gema.core.theme.GemaTheme

@Composable
fun GIcon(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    size: Dp = 24.dp,
) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = tint,
        modifier = modifier.size(size),
    )
}

@PreviewLightDark
@Composable
private fun GIconPreview() {
    GemaTheme {
        GIcon(icon = Icons.Filled.Check, tint = MaterialTheme.colorScheme.primary, size = 18.dp)
    }
}
