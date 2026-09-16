package com.emm.gema.core.ui

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.emm.gema.core.theme.GemaSpacing
import com.emm.gema.core.theme.GemaTheme

@Composable
fun GCard(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    content: @Composable ColumnScope.() -> Unit,
) {
    GBorderedContainer(
        modifier = modifier,
        containerColor = containerColor,
        contentPadding = PaddingValues(GemaSpacing.medium),
        content = content,
    )
}

@PreviewLightDark
@Composable
private fun GCardPreview() {
    GemaTheme {
        GCard {
            GText(text = "Último respaldo", style = GTextStyle.LABEL_SMALL)
            GText(text = "Hace 2 días", style = GTextStyle.TITLE_MEDIUM)
        }
    }
}
