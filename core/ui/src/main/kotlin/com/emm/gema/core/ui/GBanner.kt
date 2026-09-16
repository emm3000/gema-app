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
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.layout
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.emm.gema.core.theme.GemaAccents
import com.emm.gema.core.theme.GemaShapes
import com.emm.gema.core.theme.GemaSpacing
import com.emm.gema.core.theme.GemaTheme

private val bannerIconTopOffset = 2.dp
private val bannerDotTopOffset = 7.dp

@Composable
fun GBanner(
    text: String?,
    modifier: Modifier = Modifier,
    title: String? = null,
    tone: GBannerTone = GBannerTone.INFO,
    icon: ImageVector? = null,
    hasLeadingDot: Boolean = false,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    actionStyle: GBannerActionStyle = GBannerActionStyle.BUTTON,
    message: (@Composable () -> Unit)? = null,
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
            verticalAlignment = Alignment.Top,
        ) {
            GBannerLeading(icon = icon, hasLeadingDot = hasLeadingDot, tone = tone)
            Column(modifier = Modifier.weight(1f)) {
                GBannerMessage(title = title, text = text, message = message)
                GBannerColumnAction(actionText = actionText, onActionClick = onActionClick, actionStyle = actionStyle)
            }
            GBannerSideAction(
                actionText = actionText,
                onActionClick = onActionClick,
                actionStyle = actionStyle,
                modifier = Modifier.align(Alignment.CenterVertically),
            )
        }
    }
}

@Composable
private fun GBannerLeading(icon: ImageVector?, hasLeadingDot: Boolean, tone: GBannerTone) {
    if (icon != null) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.padding(top = bannerIconTopOffset).size(20.dp),
        )
    } else if (hasLeadingDot) {
        Box(
            modifier = Modifier
                .padding(top = bannerDotTopOffset)
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
private fun GBannerSideAction(
    actionText: String?,
    onActionClick: (() -> Unit)?,
    actionStyle: GBannerActionStyle,
    modifier: Modifier = Modifier,
) {
    if (actionText != null && onActionClick != null && actionStyle == GBannerActionStyle.LINK) {
        GBannerLinkAction(text = actionText, onClick = onActionClick, modifier = modifier)
    }
}

@Composable
private fun GBannerMessage(title: String?, text: String?, message: (@Composable () -> Unit)?) {
    if (message != null) {
        message()
    } else if (title != null) {
        Text(text = title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
        if (text != null) Text(text = text, style = MaterialTheme.typography.bodySmall)
    } else if (text != null) {
        Text(text = text, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun GBannerLinkAction(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val style: TextStyle = MaterialTheme.typography.labelLarge.copy(textDecoration = TextDecoration.Underline)
    val textMeasurer: TextMeasurer = rememberTextMeasurer()
    Box(
        modifier = modifier
            .layoutAtSizeOf { constraints: Constraints ->
                textMeasurer.measure(text = text, style = style, constraints = constraints).size
            }
            .clickable(role = Role.Button, onClick = onClick)
            .sizeIn(minWidth = GemaSpacing.minimumTouchTarget, minHeight = GemaSpacing.minimumTouchTarget),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = text, style = style, color = MaterialTheme.colorScheme.primary)
    }
}

private fun Modifier.layoutAtSizeOf(
    measureContent: (Constraints) -> IntSize,
): Modifier = layout { measurable, constraints ->
    val looseConstraints: Constraints = constraints.copy(minWidth = 0, minHeight = 0)
    val reportedSize: IntSize = measureContent(looseConstraints)
    val placeable: Placeable = measurable.measure(looseConstraints.copy(maxHeight = Constraints.Infinity))
    val placeableSize: IntSize = IntSize(placeable.width, placeable.height)
    val contentOffset: IntOffset = Alignment.Center.align(reportedSize, placeableSize, layoutDirection)
    layout(reportedSize.width, reportedSize.height) {
        placeable.place(-contentOffset)
    }
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
private fun GBannerMessageSlotPreview() {
    GemaTheme {
        GBanner(
            text = null,
            tone = GBannerTone.ERROR,
            hasLeadingDot = true,
            message = {
                GText(text = "ÚLTIMO RESPALDO", style = GTextStyle.LABEL_SMALL)
                Row {
                    GText(text = "hace 12 días", modifier = Modifier.alignByBaseline(), style = GTextStyle.NUMERAL)
                    GText(text = " · 29/08/2026", modifier = Modifier.alignByBaseline(), style = GTextStyle.BODY_MEDIUM)
                }
            },
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
private fun GBannerIconMultilinePreview() {
    GemaTheme {
        GBanner(
            text = "Viene de la plantilla SIAGIE. Si cambias el código, la exportación no lo encontrará. " +
                "Corrígelo antes de exportar el registro final.",
            icon = Icons.Filled.Info,
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
