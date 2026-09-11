package com.emm.gema.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.emm.gema.core.theme.GemaShapes
import com.emm.gema.core.theme.GemaSpacing
import com.emm.gema.core.theme.GemaTheme

@Composable
fun GFileCard(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Filled.Description,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = GemaShapes.container,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Row(
            modifier = Modifier.padding(GemaSpacing.medium),
            horizontalArrangement = Arrangement.spacedBy(GemaSpacing.small),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Column {
                GText(text = title, style = GTextStyle.TITLE_SMALL)
                GText(
                    text = subtitle,
                    style = GTextStyle.BODY_MEDIUM,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun GFileCardPreview() {
    GemaTheme {
        GFileCard(
            title = "3 Primaria EBR.xlsx",
            subtitle = "3ro A · 30 alumnos en el archivo",
        )
    }
}
