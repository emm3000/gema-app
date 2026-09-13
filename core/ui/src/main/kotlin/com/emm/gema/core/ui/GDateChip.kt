package com.emm.gema.core.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.emm.gema.core.theme.GemaShapes
import com.emm.gema.core.theme.GemaSpacing
import com.emm.gema.core.theme.GemaTheme

@Composable
fun GDateChip(text: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = GemaShapes.chip,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(horizontal = GemaSpacing.small, vertical = GemaSpacing.extraSmall),
        )
    }
}

@PreviewLightDark
@Composable
private fun GDateChipPreview() {
    GemaTheme {
        GDateChip(text = "22/06")
    }
}
