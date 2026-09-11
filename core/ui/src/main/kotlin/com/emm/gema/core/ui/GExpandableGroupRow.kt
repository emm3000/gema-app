package com.emm.gema.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.emm.gema.core.theme.GemaSpacing
import com.emm.gema.core.theme.GemaTheme

@Composable
fun GExpandableGroupRow(
    title: String,
    count: Int,
    isExpanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showDivider: Boolean = true,
) {
    Surface(onClick = onClick, modifier = modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface) {
        Column {
            Row(
                modifier = Modifier
                    .height(GemaSpacing.compactRowHeight)
                    .padding(horizontal = GemaSpacing.medium),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                GText(text = title, style = GTextStyle.BODY_LARGE)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(GemaSpacing.small),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    GText(text = count.toString(), style = GTextStyle.TITLE_MEDIUM_EMPHASIS)
                    Icon(
                        imageVector = if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (showDivider) {
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun GExpandableGroupRowPreview() {
    GemaTheme {
        Column {
            GExpandableGroupRow(title = "Se crearán", count = 4, isExpanded = false, onClick = {})
            GExpandableGroupRow(
                title = "Se propondrán como retirados",
                count = 2,
                isExpanded = true,
                showDivider = false,
                onClick = {},
            )
        }
    }
}
