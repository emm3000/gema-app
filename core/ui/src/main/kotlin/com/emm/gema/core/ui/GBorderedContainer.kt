package com.emm.gema.core.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.emm.gema.core.theme.GemaBorder
import com.emm.gema.core.theme.GemaShapes
import com.emm.gema.core.theme.GemaTheme

@Composable
fun GBorderedContainer(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = GemaShapes.container,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(GemaBorder.hairline, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(content = content)
    }
}

@PreviewLightDark
@Composable
private fun GBorderedContainerPreview() {
    GemaTheme {
        GBorderedContainer {
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
                title = "Se actualizarán",
                count = 26,
                isExpanded = false,
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
