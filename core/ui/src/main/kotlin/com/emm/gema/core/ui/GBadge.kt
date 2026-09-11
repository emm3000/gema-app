@file:Suppress("MatchingDeclarationName")

package com.emm.gema.core.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.emm.gema.core.theme.GemaShapes
import com.emm.gema.core.theme.GemaSpacing
import com.emm.gema.core.theme.GemaTheme

enum class GBadgeTone {
    PRIMARY,
    ERROR,
}

@Composable
fun GBadge(text: String, modifier: Modifier = Modifier, tone: GBadgeTone = GBadgeTone.PRIMARY) {
    val color: Color = when (tone) {
        GBadgeTone.PRIMARY -> MaterialTheme.colorScheme.primaryContainer
        GBadgeTone.ERROR -> MaterialTheme.colorScheme.errorContainer
    }
    val contentColor: Color = when (tone) {
        GBadgeTone.PRIMARY -> MaterialTheme.colorScheme.onPrimaryContainer
        GBadgeTone.ERROR -> MaterialTheme.colorScheme.onErrorContainer
    }
    Surface(
        modifier = modifier,
        shape = GemaShapes.control,
        color = color,
        contentColor = contentColor,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = GemaSpacing.small, vertical = GemaSpacing.extraSmall),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
        )
    }
}

@PreviewLightDark
@Composable
private fun GBadgePreview() {
    GemaTheme {
        GBadge(text = "ACTIVO")
    }
}

@PreviewLightDark
@Composable
private fun GBadgeErrorPreview() {
    GemaTheme {
        GBadge(text = "12 faltan", tone = GBadgeTone.ERROR)
    }
}
