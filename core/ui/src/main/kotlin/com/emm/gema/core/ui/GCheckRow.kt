package com.emm.gema.core.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.emm.gema.core.theme.GemaSpacing
import com.emm.gema.core.theme.GemaTheme

@Composable
fun GCheckRow(
    title: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    prefix: String? = null,
    subtitle: String? = null,
    subtitleColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    isEnabled: Boolean = true,
    showBottomDivider: Boolean = false,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = GemaSpacing.minimumTouchTarget)
                .toggleable(
                    value = isChecked,
                    enabled = isEnabled,
                    onValueChange = onCheckedChange,
                    role = Role.Checkbox,
                )
                .padding(horizontal = GemaSpacing.medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(checked = isChecked, onCheckedChange = null, enabled = isEnabled)
            if (prefix != null) {
                Text(
                    text = prefix,
                    modifier = Modifier
                        .padding(start = GemaSpacing.small)
                        .widthIn(min = GemaSpacing.large),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column(
                modifier = Modifier
                    .padding(start = GemaSpacing.small)
                    .weight(1f),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = subtitleColor,
                    )
                }
            }
        }
        if (showBottomDivider) {
            GDivider()
        }
    }
}

@PreviewLightDark
@Composable
private fun GCheckRowPreview() {
    GemaTheme {
        GCheckRow(
            title = "Construye su identidad",
            isChecked = true,
            onCheckedChange = {},
            prefix = "01",
            subtitle = "12 niveles registrados",
        )
    }
}
