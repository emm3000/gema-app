package com.emm.gema.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.emm.gema.core.theme.GemaTheme

@Composable
fun GCircledIcon(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    containerSize: Dp = 48.dp,
    iconSize: Dp = 24.dp,
) {
    Box(
        modifier = modifier
            .size(containerSize)
            .background(color = containerColor, shape = CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        GIcon(icon = icon, tint = tint, size = iconSize)
    }
}

@PreviewLightDark
@Composable
private fun GCircledIconPreview() {
    GemaTheme {
        GCircledIcon(
            icon = Icons.Filled.DateRange,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}
