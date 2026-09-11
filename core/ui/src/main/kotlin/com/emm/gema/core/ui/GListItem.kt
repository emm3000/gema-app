package com.emm.gema.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.emm.gema.core.theme.GemaSpacing
import com.emm.gema.core.theme.GemaTheme

@Composable
fun GListItem(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leadingText: String? = null,
    trailingText: String? = null,
    hasChevron: Boolean = false,
    isExpanded: Boolean? = null,
    showDivider: Boolean = true,
    titleStyle: GTextStyle = GTextStyle.BODY_LARGE,
    isEmphasized: Boolean = false,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    val containerColor = if (isEmphasized) {
        MaterialTheme.colorScheme.surfaceVariant
    } else {
        MaterialTheme.colorScheme.surface
    }
    if (onClick != null) {
        Surface(onClick = onClick, modifier = modifier, color = containerColor) {
            GListItemBody(
                title = title,
                subtitle = subtitle,
                leadingText = leadingText,
                trailingText = trailingText,
                hasChevron = hasChevron,
                isExpanded = isExpanded,
                showDivider = showDivider,
                titleStyle = titleStyle,
                trailing = trailing,
            )
        }
    } else {
        Surface(modifier = modifier, color = containerColor) {
            GListItemBody(
                title = title,
                subtitle = subtitle,
                leadingText = leadingText,
                trailingText = trailingText,
                hasChevron = hasChevron,
                isExpanded = isExpanded,
                showDivider = showDivider,
                titleStyle = titleStyle,
                trailing = trailing,
            )
        }
    }
}

@Composable
private fun GListItemBody(
    title: String,
    subtitle: String?,
    leadingText: String?,
    trailingText: String?,
    hasChevron: Boolean,
    isExpanded: Boolean?,
    showDivider: Boolean,
    titleStyle: GTextStyle,
    trailing: (@Composable () -> Unit)?,
) {
    val subtitleContent: (@Composable () -> Unit)? = subtitle?.let { text ->
        {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
    val leadingContent: (@Composable () -> Unit)? = leadingText?.let { text ->
        {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.width(GemaSpacing.leadingLabelWidth),
            )
        }
    }
    val trailingContent: (@Composable () -> Unit)? = gListItemTrailing(
        trailingText = trailingText,
        hasChevron = hasChevron,
        isExpanded = isExpanded,
        trailing = trailing,
    )

    Column {
        ListItem(
            leadingContent = leadingContent,
            headlineContent = {
                Text(
                    text = title,
                    style = titleStyle.toTextStyle(),
                    color = MaterialTheme.colorScheme.onSurface,
                )
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
    isExpanded: Boolean?,
    trailing: (@Composable () -> Unit)?,
): (@Composable () -> Unit)? {
    val hasTrailingContent: Boolean = trailingText != null || hasChevron || isExpanded != null || trailing != null
    if (!hasTrailingContent) return null
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
            if (isExpanded != null) {
                Icon(
                    imageVector = if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
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
