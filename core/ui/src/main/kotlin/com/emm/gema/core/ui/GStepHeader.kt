@file:Suppress("MatchingDeclarationName")

package com.emm.gema.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.emm.gema.core.theme.GemaSpacing
import com.emm.gema.core.theme.GemaTheme

@Composable
fun GStepHeader(
    step: String,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(GemaSpacing.extraSmall)) {
        GText(text = step, style = GTextStyle.LABEL_SMALL, color = MaterialTheme.colorScheme.onSurfaceVariant)
        GText(text = title, style = GTextStyle.TITLE_MEDIUM)
        GText(text = description, style = GTextStyle.BODY_LARGE)
    }
}

@PreviewLightDark
@Composable
private fun GStepHeaderPreview() {
    GemaTheme {
        GStepHeader(
            step = "Paso 1 de 2",
            title = "Tu año escolar",
            description = "Solo lo que necesitamos para empezar. Todo se puede corregir después.",
        )
    }
}
