package com.emm.gema.core.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.emm.gema.core.theme.GemaTheme

@Composable
fun GText(
    text: String,
    modifier: Modifier = Modifier,
    style: GTextStyle = GTextStyle.BODY_MEDIUM,
    color: Color = Color.Unspecified,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    val resolvedStyle: TextStyle = style.toTextStyle()
    Text(
        text = text,
        modifier = modifier,
        style = resolvedStyle,
        color = color,
        maxLines = maxLines,
        overflow = overflow,
    )
}

@Composable
private fun GTextStyle.toTextStyle(): TextStyle = when (this) {
    GTextStyle.TITLE_MEDIUM -> MaterialTheme.typography.titleMedium
    GTextStyle.TITLE_SMALL -> MaterialTheme.typography.titleSmall
    GTextStyle.BODY_LARGE -> MaterialTheme.typography.bodyLarge
    GTextStyle.BODY_MEDIUM -> MaterialTheme.typography.bodyMedium
    GTextStyle.BODY_SMALL -> MaterialTheme.typography.bodySmall
    GTextStyle.LABEL_MEDIUM -> MaterialTheme.typography.labelMedium
    GTextStyle.LABEL_SMALL -> MaterialTheme.typography.labelSmall
}

@PreviewLightDark
@Composable
private fun GTextPreview() {
    GemaTheme {
        Column {
            GText(text = "Título de sección", style = GTextStyle.TITLE_MEDIUM)
            GText(text = "Cuerpo de texto", style = GTextStyle.BODY_MEDIUM)
            GText(text = "Etiqueta", style = GTextStyle.LABEL_SMALL)
        }
    }
}
