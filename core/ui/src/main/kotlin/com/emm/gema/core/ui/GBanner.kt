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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.emm.gema.core.theme.GemaShapes
import com.emm.gema.core.theme.GemaSpacing
import com.emm.gema.core.theme.GemaTheme

@Composable
fun GBanner(
    text: String,
    modifier: Modifier = Modifier,
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
                Text(text = text, style = MaterialTheme.typography.bodyMedium)
                if (actionText != null && onActionClick != null) {
                    Spacer(modifier = Modifier.height(GemaSpacing.small))
                    when (actionStyle) {
                        GBannerActionStyle.BUTTON -> GButton(
                            text = actionText,
                            onClick = onActionClick,
                            variant = GButtonVariant.TEXT,
                        )
                        GBannerActionStyle.LINK -> Row(
                            modifier = Modifier.clickable(onClick = onActionClick),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = actionText,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }
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
