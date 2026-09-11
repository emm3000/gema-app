package com.emm.gema.core.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.emm.gema.core.theme.GemaShapes
import com.emm.gema.core.theme.GemaSpacing
import com.emm.gema.core.theme.GemaTheme

@Composable
fun GBanner(
    text: String,
    modifier: Modifier = Modifier,
    tone: GBannerTone = GBannerTone.INFO,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = GemaShapes.container,
        color = containerColorOf(tone),
        contentColor = contentColorOf(tone),
    ) {
        Column(modifier = Modifier.padding(GemaSpacing.medium)) {
            Text(text = text, style = MaterialTheme.typography.bodyMedium)
            if (actionText != null && onActionClick != null) {
                Spacer(modifier = Modifier.height(GemaSpacing.small))
                GButton(
                    text = actionText,
                    onClick = onActionClick,
                    variant = GButtonVariant.TEXT,
                )
            }
        }
    }
}

@Composable
private fun containerColorOf(tone: GBannerTone): Color = when (tone) {
    GBannerTone.INFO, GBannerTone.WARNING -> MaterialTheme.colorScheme.surfaceVariant
    GBannerTone.ERROR -> MaterialTheme.colorScheme.errorContainer
}

@Composable
private fun contentColorOf(tone: GBannerTone): Color = when (tone) {
    GBannerTone.INFO, GBannerTone.WARNING -> MaterialTheme.colorScheme.onSurfaceVariant
    GBannerTone.ERROR -> MaterialTheme.colorScheme.onErrorContainer
}

@PreviewLightDark
@Composable
private fun GBannerPreview() {
    GemaTheme {
        GBanner(
            text = "Último respaldo hace 12 días",
            tone = GBannerTone.WARNING,
            actionText = "Respaldar ahora",
            onActionClick = {},
        )
    }
}
