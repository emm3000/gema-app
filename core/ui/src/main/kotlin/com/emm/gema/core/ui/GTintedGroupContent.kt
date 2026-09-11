package com.emm.gema.core.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.emm.gema.core.theme.GemaTheme

@Composable
fun GTintedGroupContent(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column {
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            content()
        }
    }
}

@PreviewLightDark
@Composable
private fun GTintedGroupContentPreview() {
    GemaTheme {
        GTintedGroupContent {
            GCheckRow(title = "LÓPEZ SILVA, Ana", isChecked = true, onCheckedChange = {})
        }
    }
}
