package com.emm.gema.core.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.emm.gema.core.theme.GemaTheme

@Composable
fun GText(
    text: String,
    modifier: Modifier = Modifier,
    style: GTextStyle = GTextStyle.BODY_LARGE,
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
fun GTextStyle.toTextStyle(): TextStyle = when (this) {
    GTextStyle.TITLE_MEDIUM -> MaterialTheme.typography.titleMedium
    GTextStyle.TITLE_MEDIUM_EMPHASIS -> MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
    GTextStyle.NUMERAL -> MaterialTheme.typography.titleLarge.copy(
        fontWeight = FontWeight.SemiBold,
        fontFeatureSettings = "tnum",
    )
    GTextStyle.BODY_LARGE -> MaterialTheme.typography.bodyLarge
    GTextStyle.BODY_LARGE_EMPHASIS -> MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
    GTextStyle.BODY_MEDIUM -> MaterialTheme.typography.bodyMedium
    GTextStyle.BODY_SMALL -> MaterialTheme.typography.bodySmall
    GTextStyle.BODY_SMALL_EMPHASIS -> MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
    GTextStyle.BODY_SMALL_TABULAR -> MaterialTheme.typography.bodySmall.copy(fontFeatureSettings = "tnum")
    GTextStyle.LABEL_LARGE_EMPHASIS -> MaterialTheme.typography.labelLarge.copy(
        fontWeight = FontWeight.SemiBold,
    )
    GTextStyle.LABEL_SMALL -> MaterialTheme.typography.labelSmall
}

@PreviewLightDark
@Composable
private fun GTextPreview() {
    GemaTheme {
        Column {
            GText(text = "Título de sección", style = GTextStyle.TITLE_MEDIUM)
            GText(text = "Cuerpo de texto", style = GTextStyle.BODY_LARGE)
            GText(text = "Etiqueta", style = GTextStyle.LABEL_SMALL)
            GText(text = "24/30", style = GTextStyle.LABEL_LARGE_EMPHASIS)
            GText(text = "26", style = GTextStyle.TITLE_MEDIUM_EMPHASIS)
            GText(text = "28", style = GTextStyle.NUMERAL)
        }
    }
}
