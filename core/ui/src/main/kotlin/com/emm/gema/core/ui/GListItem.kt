package com.emm.gema.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.emm.gema.core.theme.GemaSpacing
import com.emm.gema.core.theme.GemaTheme

@Composable
fun GListItem(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    trailingText: String? = null,
    hasChevron: Boolean = false,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    if (onClick != null) {
        Surface(onClick = onClick, modifier = modifier, color = MaterialTheme.colorScheme.surface) {
            GListItemBody(
                title = title,
                subtitle = subtitle,
                trailingText = trailingText,
                hasChevron = hasChevron,
                trailing = trailing,
            )
        }
    } else {
        Surface(modifier = modifier, color = MaterialTheme.colorScheme.surface) {
            GListItemBody(
                title = title,
                subtitle = subtitle,
                trailingText = trailingText,
                hasChevron = hasChevron,
                trailing = trailing,
            )
        }
    }
}

@Composable
private fun GListItemBody(
    title: String,
    subtitle: String?,
    trailingText: String?,
    hasChevron: Boolean,
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
    val trailingContent: (@Composable () -> Unit)? = gListItemTrailing(
        trailingText = trailingText,
        hasChevron = hasChevron,
        trailing = trailing,
    )

    Column {
        ListItem(
            headlineContent = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            },
            supportingContent = subtitleContent,
            trailingContent = trailingContent,
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
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
