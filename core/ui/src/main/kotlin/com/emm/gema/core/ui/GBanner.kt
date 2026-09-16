package com.emm.gema.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    hasLeadingDot: Boolean = false,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    actionStyle: GBannerActionStyle = GBannerActionStyle.BUTTON,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = GemaShapes.control,
        color = containerColorOf(tone),
        contentColor = contentColorOf(tone),
    ) {
        Row(
            modifier = Modifier.padding(GemaSpacing.medium).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(GemaSpacing.small),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GBannerLeading(icon = icon, hasLeadingDot = hasLeadingDot, tone = tone)
            Column(modifier = Modifier.weight(1f)) {
                GBannerMessage(title = title, text = text)
                GBannerColumnAction(actionText = actionText, onActionClick = onActionClick, actionStyle = actionStyle)
            }
            GBannerSideAction(actionText = actionText, onActionClick = onActionClick, actionStyle = actionStyle)
        }
    }
}

@Composable
private fun GBannerLeading(icon: ImageVector?, hasLeadingDot: Boolean, tone: GBannerTone) {
    if (icon != null) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp))
    } else if (hasLeadingDot) {
        Box(
            modifier = Modifier
                .size(GemaSpacing.small)
                .clip(CircleShape)
                .background(dotColorOf(tone)),
        )
    }
}

@Composable
private fun GBannerColumnAction(actionText: String?, onActionClick: (() -> Unit)?, actionStyle: GBannerActionStyle) {
    if (actionText == null || onActionClick == null) return
    when (actionStyle) {
        GBannerActionStyle.BUTTON -> {
            Spacer(modifier = Modifier.height(GemaSpacing.small))
            GButton(text = actionText, onClick = onActionClick, variant = GButtonVariant.TEXT)
        }
        GBannerActionStyle.STACKED_LINK -> GBannerStackedLinkAction(text = actionText, onClick = onActionClick)
        GBannerActionStyle.LINK -> Unit
    }
}

@Composable
private fun GBannerSideAction(actionText: String?, onActionClick: (() -> Unit)?, actionStyle: GBannerActionStyle) {
    if (actionText != null && onActionClick != null && actionStyle == GBannerActionStyle.LINK) {
        GBannerLinkAction(text = actionText, onClick = onActionClick)
    }
}

@Composable
private fun GBannerMessage(title: String?, text: String) {
    if (title != null) {
        Text(text = title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
        Text(text = text, style = MaterialTheme.typography.bodySmall)
    } else {
        Text(text = text, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun GBannerLinkAction(text: String, onClick: () -> Unit) {
    Text(
        text = text,
        modifier = Modifier.clickable(onClick = onClick),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        textDecoration = TextDecoration.Underline,
    )
}

@Composable
private fun GBannerStackedLinkAction(text: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = GemaSpacing.minimumTouchTarget),
        color = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.primary,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = text,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline,
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun containerColorOf(tone: GBannerTone): Color = when (tone) {
    GBannerTone.INFO -> MaterialTheme.colorScheme.surfaceContainerLow
    GBannerTone.WARNING -> GemaAccents.warningContainer
    GBannerTone.ERROR -> MaterialTheme.colorScheme.errorContainer
}

@Composable
private fun contentColorOf(tone: GBannerTone): Color = when (tone) {
    GBannerTone.INFO -> MaterialTheme.colorScheme.onSurfaceVariant
    GBannerTone.WARNING -> GemaAccents.onWarningContainer
    GBannerTone.ERROR -> MaterialTheme.colorScheme.onErrorContainer
}

@Composable
private fun dotColorOf(tone: GBannerTone): Color = when (tone) {
    GBannerTone.ERROR -> MaterialTheme.colorScheme.error
    else -> contentColorOf(tone)
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

@PreviewLightDark
@Composable
private fun GBannerDotLinkPreview() {
    GemaTheme {
        GBanner(
            text = "Último respaldo hace 12 días",
            tone = GBannerTone.ERROR,
            hasLeadingDot = true,
            actionText = "Respaldar",
            onActionClick = {},
            actionStyle = GBannerActionStyle.LINK,
        )
    }
}

@PreviewLightDark
@Composable
private fun GBannerStackedLinkPreview() {
    GemaTheme {
        GBanner(
            text = "Todas las áreas quedan activas.",
            actionText = "No dicto todas las áreas",
            onActionClick = {},
            actionStyle = GBannerActionStyle.STACKED_LINK,
        )
    }
}

@PreviewLightDark
@Composable
private fun GBannerStackedLinkLongLabelPreview() {
    GemaTheme {
        GBanner(
            text = "Todas las áreas quedan activas.",
            actionText = "No dicto todas las áreas curriculares de este grado en esta sección",
            onActionClick = {},
            actionStyle = GBannerActionStyle.STACKED_LINK,
        )
    }
}
