package com.emm.gema.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.emm.gema.core.theme.GemaBorder
import com.emm.gema.core.theme.GemaShapes
import com.emm.gema.core.theme.GemaSpacing
import com.emm.gema.core.theme.GemaTheme

private val pendingDashLength: Dp = 6.dp
private val pendingDashGap: Dp = 4.dp

enum class GAttendanceOption(val label: String, val contentDescription: String) {
    PRESENT("P", "Presente"),
    LATE("T", "Tardanza"),
    ABSENT("F", "Falta"),
    JUSTIFIED("FJ", "Falta justificada"),
}

@Composable
fun GAttendanceToggle(
    option: GAttendanceOption,
    isRecorded: Boolean,
    onSelect: (GAttendanceOption) -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape: Shape = GemaShapes.control
    val outline: Color = MaterialTheme.colorScheme.outline
    val dividerColor: Color = if (isRecorded) outline else MaterialTheme.colorScheme.outlineVariant

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(GemaSpacing.minimumTouchTarget)
            .clip(shape)
            .then(
                if (isRecorded) {
                    Modifier.border(GemaBorder.hairline, outline, shape)
                } else {
                    Modifier.pendingOutline(outline)
                },
            ),
    ) {
        GAttendanceOption.entries.forEachIndexed { index: Int, entry: GAttendanceOption ->
            if (index > 0) {
                VerticalDivider(color = dividerColor, thickness = GemaBorder.hairline)
            }
            val isSelected: Boolean = isRecorded && entry == option
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(containerColorFor(entry, isSelected))
                    .selectable(
                        selected = isSelected,
                        onClick = { onSelect(if (isSelected) option else entry) },
                        role = Role.RadioButton,
                    )
                    .semantics { contentDescription = entry.contentDescription },
                contentAlignment = Alignment.Center,
            ) {
                GText(
                    text = entry.label,
                    style = if (isSelected) GTextStyle.LABEL_LARGE_EMPHASIS else GTextStyle.BODY_LARGE,
                    color = contentColorFor(entry, isSelected),
                )
            }
        }
    }
}

@Composable
private fun containerColorFor(option: GAttendanceOption, isSelected: Boolean): Color = when {
    !isSelected -> MaterialTheme.colorScheme.surface
    option == GAttendanceOption.PRESENT -> MaterialTheme.colorScheme.primaryContainer
    option == GAttendanceOption.LATE -> MaterialTheme.colorScheme.surfaceVariant
    option == GAttendanceOption.ABSENT -> MaterialTheme.colorScheme.errorContainer
    else -> MaterialTheme.colorScheme.inverseSurface
}

@Composable
private fun contentColorFor(option: GAttendanceOption, isSelected: Boolean): Color = when {
    !isSelected -> MaterialTheme.colorScheme.onSurface
    option == GAttendanceOption.PRESENT -> MaterialTheme.colorScheme.onPrimaryContainer
    option == GAttendanceOption.LATE -> MaterialTheme.colorScheme.onSurfaceVariant
    option == GAttendanceOption.ABSENT -> MaterialTheme.colorScheme.onErrorContainer
    else -> MaterialTheme.colorScheme.inverseOnSurface
}

private fun Modifier.pendingOutline(color: Color): Modifier = drawBehind {
    drawRoundRect(
        color = color,
        cornerRadius = CornerRadius(GemaShapes.controlRadius.toPx()),
        style = Stroke(
            width = GemaBorder.hairline.toPx(),
            pathEffect = PathEffect.dashPathEffect(
                floatArrayOf(pendingDashLength.toPx(), pendingDashGap.toPx()),
            ),
        ),
    )
}

@PreviewLightDark
@Composable
private fun GAttendanceTogglePreview() {
    GemaTheme {
        GAttendanceToggle(option = GAttendanceOption.PRESENT, isRecorded = true, onSelect = {})
    }
}

@PreviewLightDark
@Composable
private fun GAttendanceTogglePendingPreview() {
    GemaTheme {
        GAttendanceToggle(option = GAttendanceOption.PRESENT, isRecorded = false, onSelect = {})
    }
}
