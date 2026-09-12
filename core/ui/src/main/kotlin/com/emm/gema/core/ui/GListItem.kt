package com.emm.gema.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.emm.gema.core.theme.GemaSpacing
import com.emm.gema.core.theme.GemaTheme

@Composable
fun GListItem(
    title: String,
    modifier: Modifier = Modifier,
    titleStyle: GTextStyle = GTextStyle.BODY_LARGE,
    subtitle: String? = null,
    subtitleColor: Color? = null,
    leadingText: String? = null,
    leadingIcon: ImageVector? = null,
    titleLeading: (@Composable () -> Unit)? = null,
    trailingText: String? = null,
    hasChevron: Boolean = false,
    showDivider: Boolean = true,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    val heightModifier: Modifier = modifier.heightIn(min = GemaSpacing.minimumTouchTarget)
    if (onClick != null) {
        Surface(onClick = onClick, modifier = heightModifier, color = MaterialTheme.colorScheme.surface) {
            GListItemBody(
                title = title,
                titleStyle = titleStyle,
                subtitle = subtitle,
                subtitleColor = subtitleColor,
                leadingText = leadingText,
                leadingIcon = leadingIcon,
                titleLeading = titleLeading,
                trailingText = trailingText,
                hasChevron = hasChevron,
                showDivider = showDivider,
                trailing = trailing,
            )
        }
    } else {
        Surface(modifier = heightModifier, color = MaterialTheme.colorScheme.surface) {
            GListItemBody(
                title = title,
                titleStyle = titleStyle,
                subtitle = subtitle,
                subtitleColor = subtitleColor,
                leadingText = leadingText,
                leadingIcon = leadingIcon,
                titleLeading = titleLeading,
                trailingText = trailingText,
                hasChevron = hasChevron,
                showDivider = showDivider,
                trailing = trailing,
            )
        }
    }
}

@Composable
private fun GListItemBody(
    title: String,
    titleStyle: GTextStyle,
    subtitle: String?,
    subtitleColor: Color?,
    leadingText: String?,
    leadingIcon: ImageVector?,
    titleLeading: (@Composable () -> Unit)?,
    trailingText: String?,
    hasChevron: Boolean,
    showDivider: Boolean,
    trailing: (@Composable () -> Unit)?,
) {
    val subtitleContent: (@Composable () -> Unit)? = subtitle?.let { text ->
        {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = subtitleColor ?: MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
    val leadingContent: (@Composable () -> Unit)? = when {
        leadingIcon != null -> {
            {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        leadingText != null -> {
            {
                Text(
                    text = leadingText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.width(GemaSpacing.leadingLabelWidth),
                )
            }
        }
        else -> null
    }
    val trailingContent: (@Composable () -> Unit)? = gListItemTrailing(
        trailingText = trailingText,
        hasChevron = hasChevron,
        trailing = trailing,
    )

    Column {
        ListItem(
            leadingContent = leadingContent,
            headlineContent = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(GemaSpacing.small),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    titleLeading?.invoke()
                    Text(
                        text = title,
                        style = titleStyle.toTextStyle(),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            },
            supportingContent = subtitleContent,
            trailingContent = trailingContent,
        )
        if (showDivider) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
        }
    }
}

private fun gListItemTrailing(
    trailingText: String?,
    hasChevron: Boolean,
    trailing: (@Composable () -> Unit)?,
): (@Composable () -> Unit)? {
    if (trailingText == null && !hasChevron && trailing == null) return null
    return {
        Row(
            horizontalArrangement = Arrangement.spacedBy(GemaSpacing.small),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (trailingText != null) {
                Text(
                    text = trailingText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            trailing?.invoke()
            if (hasChevron) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun GListItemPreview() {
    GemaTheme {
        GListItem(
            title = "CCAHUANA MAMANI, Rosa Elena",
            subtitle = "Código 001",
            trailingText = "18",
            hasChevron = true,
            onClick = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun GListItemGroupedPreview() {
    GemaTheme {
        Column {
            GListItem(title = "01 mar – 15 may", leadingText = "I", onClick = {})
            GListItem(title = "18 may – 31 jul", leadingText = "II", showDivider = false, onClick = {})
        }
    }
}
