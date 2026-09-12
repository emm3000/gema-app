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
import androidx.compose.ui.unit.sp
import com.emm.gema.core.theme.GemaTheme
import com.emm.gema.core.theme.gemaCardDateFontSize
import com.emm.gema.core.theme.gemaCardTitleFontSize

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
internal fun GTextStyle.toTextStyle(): TextStyle = when (this) {
    GTextStyle.TITLE_MEDIUM -> MaterialTheme.typography.titleMedium
    GTextStyle.TITLE_MEDIUM_EMPHASIS -> MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
    GTextStyle.CARD_TITLE_EMPHASIS -> MaterialTheme.typography.titleLarge.copy(
        fontSize = gemaCardTitleFontSize,
        fontWeight = FontWeight.Bold,
    )
    GTextStyle.TITLE_SMALL -> MaterialTheme.typography.titleSmall
    GTextStyle.BODY_LARGE -> MaterialTheme.typography.bodyLarge
    GTextStyle.BODY_MEDIUM -> MaterialTheme.typography.bodyMedium
    GTextStyle.BODY_SMALL -> MaterialTheme.typography.bodySmall
    GTextStyle.LABEL_LARGE_EMPHASIS -> MaterialTheme.typography.labelLarge.copy(
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
    )
    GTextStyle.LABEL_MEDIUM -> MaterialTheme.typography.labelMedium
    GTextStyle.LABEL_SMALL -> MaterialTheme.typography.labelSmall
    GTextStyle.LABEL_SMALL_EMPHASIS -> MaterialTheme.typography.labelSmall.copy(
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
    )
    GTextStyle.TITLE_LARGE_EMPHASIS -> MaterialTheme.typography.titleLarge.copy(
        fontSize = gemaCardDateFontSize,
        fontWeight = FontWeight.Bold,
    )
}

@PreviewLightDark
@Composable
private fun GTextPreview() {
    GemaTheme {
        Column {
            GText(text = "Título de sección", style = GTextStyle.TITLE_MEDIUM)
            GText(text = "Cuerpo de texto", style = GTextStyle.BODY_MEDIUM)
            GText(text = "Etiqueta", style = GTextStyle.LABEL_SMALL)
            GText(text = "Encabezado de sección", style = GTextStyle.LABEL_SMALL_EMPHASIS)
            GText(text = "24/30", style = GTextStyle.LABEL_LARGE_EMPHASIS)
            GText(text = "26", style = GTextStyle.TITLE_MEDIUM_EMPHASIS)
            GText(text = "Martes 10 de setiembre", style = GTextStyle.TITLE_LARGE_EMPHASIS)
        }
    }
}
