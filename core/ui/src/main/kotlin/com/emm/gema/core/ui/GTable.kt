package com.emm.gema.core.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.emm.gema.core.theme.GemaSpacing
import com.emm.gema.core.theme.GemaTheme

@Composable
fun GTableHeaderBand(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Column(modifier = modifier) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Box(modifier = Modifier.fillMaxWidth().height(GemaSpacing.gridChipHeight)) {
                content()
            }
        }
        HorizontalDivider()
    }
}

@Composable
fun GTableRow(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Column(modifier = modifier) {
        Box(modifier = Modifier.fillMaxWidth().height(GemaSpacing.compactRowHeight)) {
            content()
        }
        HorizontalDivider()
    }
}

@PreviewLightDark
@Composable
private fun GTableHeaderBandPreview() {
    GemaTheme {
        GTableHeaderBand(modifier = Modifier.fillMaxWidth()) {
            GText(
                text = "ALUMNO",
                modifier = Modifier.fillMaxSize().padding(horizontal = GemaSpacing.medium),
                style = GTextStyle.LABEL_SMALL,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun GTableRowPreview() {
    GemaTheme {
        GTableRow(modifier = Modifier.fillMaxWidth()) {
            GText(
                text = "CCAHUANA MAMANI, Rosa",
                modifier = Modifier.fillMaxSize().padding(horizontal = GemaSpacing.medium),
                style = GTextStyle.BODY_LARGE,
            )
        }
    }
}
