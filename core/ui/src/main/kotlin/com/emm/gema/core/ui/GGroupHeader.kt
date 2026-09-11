package com.emm.gema.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
fun GGroupHeader(
    title: String,
    modifier: Modifier = Modifier,
    count: Int? = null,
    isExpanded: Boolean? = null,
    onClick: (() -> Unit)? = null,
) {
    val content: @Composable () -> Unit = {
        Row(
            modifier = Modifier
                .height(GemaSpacing.compactRowHeight)
                .padding(horizontal = GemaSpacing.screenGutter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GText(
                text = if (count != null) "$title ($count)" else title,
                style = GTextStyle.LABEL_SMALL_EMPHASIS,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (isExpanded != null) {
                Icon(
                    imageVector = if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            content()
        }
    } else {
        Surface(modifier = modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surfaceVariant) {
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
