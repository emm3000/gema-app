package com.emm.gema.core.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.emm.gema.core.theme.GemaAccents
import com.emm.gema.core.theme.GemaShapes
import com.emm.gema.core.theme.GemaSpacing
import com.emm.gema.core.theme.GemaTheme

@Composable
fun GBanner(
    text: String,
    modifier: Modifier = Modifier,
    title: String? = null,
    tone: GBannerTone = GBannerTone.INFO,
    icon: ImageVector? = null,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    actionStyle: GBannerActionStyle = GBannerActionStyle.BUTTON,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = GemaShapes.container,
        color = containerColorOf(tone),
        contentColor = contentColorOf(tone),
    ) {
        Row(
            modifier = Modifier.padding(GemaSpacing.medium),
            horizontalArrangement = Arrangement.spacedBy(GemaSpacing.small),
        ) {
            if (icon != null) {
                Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp))
            }
            Column {
                if (title != null) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    )
                    Text(text = text, style = MaterialTheme.typography.bodySmall)
                } else {
                    Text(text = text, style = MaterialTheme.typography.bodyMedium)
                }
                if (actionText != null && onActionClick != null) {
                    Spacer(modifier = Modifier.height(GemaSpacing.small))
                    when (actionStyle) {
                        GBannerActionStyle.BUTTON -> GButton(
                            text = actionText,
                            onClick = onActionClick,
                            variant = GButtonVariant.TEXT,
                        )
                        GBannerActionStyle.LINK -> Text(
                            text = actionText,
                            modifier = Modifier.clickable(onClick = onActionClick),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            textDecoration = TextDecoration.Underline,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun containerColorOf(tone: GBannerTone): Color = when (tone) {
    GBannerTone.INFO -> MaterialTheme.colorScheme.surfaceVariant
    GBannerTone.WARNING -> GemaAccents.warningContainer
    GBannerTone.ERROR -> MaterialTheme.colorScheme.errorContainer
}

@Composable
private fun contentColorOf(tone: GBannerTone): Color = when (tone) {
    GBannerTone.INFO -> MaterialTheme.colorScheme.onSurfaceVariant
    GBannerTone.WARNING -> GemaAccents.onWarningContainer
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
