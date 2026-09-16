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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.emm.gema.core.theme.GemaSpacing
import com.emm.gema.core.theme.GemaTheme

@Composable
fun GGroupHeader(
    title: String,
    modifier: Modifier = Modifier,
    count: Int? = null,
    isExpanded: Boolean? = null,
    onClick: (() -> Unit)? = null,
    titleStyle: GTextStyle = GTextStyle.LABEL_SMALL,
    titleColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    countInTitle: Boolean = true,
    showDivider: Boolean = false,
) {
    val content: @Composable () -> Unit = {
        Column {
            Row(
                modifier = Modifier
                    .height(GemaSpacing.compactRowHeight)
                    .padding(horizontal = GemaSpacing.screenGutter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                GText(
                    text = if (count != null && countInTitle) "$title ($count)" else title,
                    style = titleStyle,
                    color = titleColor,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(GemaSpacing.small),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (count != null && !countInTitle) {
                        GText(text = count.toString(), style = GTextStyle.TITLE_MEDIUM_EMPHASIS)
                    }
                    if (isExpanded != null) {
                        Icon(
                            imageVector = if (isExpanded) {
                                Icons.Filled.KeyboardArrowUp
                            } else {
                                Icons.Filled.KeyboardArrowDown
                            },
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            if (showDivider) {
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            }
        }
    }
    if (onClick != null) {
        Surface(onClick = onClick, modifier = modifier.fillMaxWidth(), color = containerColor) {
            content()
        }
    } else {
        Surface(modifier = modifier.fillMaxWidth(), color = containerColor) {
            content()
        }
    }
}

@PreviewLightDark
@Composable
private fun GGroupHeaderPreview() {
    GemaTheme {
        GGroupHeader(title = "RETIRADOS", count = 2, isExpanded = false, onClick = {})
    }
}

@PreviewLightDark
@Composable
private fun GGroupHeaderStaticPreview() {
    GemaTheme {
        GGroupHeader(title = "PERSONAL SOCIAL")
    }
}

@PreviewLightDark
@Composable
private fun GGroupHeaderExpandableVariantPreview() {
    GemaTheme {
        Column {
            GGroupHeader(
                title = "Se crearán",
                count = 4,
                isExpanded = false,
                onClick = {},
                titleStyle = GTextStyle.BODY_LARGE,
                titleColor = Color.Unspecified,
                containerColor = MaterialTheme.colorScheme.surface,
                countInTitle = false,
                showDivider = true,
            )
            GGroupHeader(
                title = "Se propondrán como retirados",
                count = 2,
                isExpanded = true,
                onClick = {},
                titleStyle = GTextStyle.BODY_LARGE,
                titleColor = Color.Unspecified,
                containerColor = MaterialTheme.colorScheme.surface,
                countInTitle = false,
                showDivider = false,
            )
        }
    }
}
