package com.emm.gema.core.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.emm.gema.core.theme.GemaBorder
import com.emm.gema.core.theme.GemaTheme

private val pendingCornerRadius: Dp = 12.dp
private val pendingDashLength: Dp = 6.dp
private val pendingDashGap: Dp = 4.dp

enum class GAttendanceOption(val label: String, val contentDescription: String) {
    PRESENT("P", "Presente"),
    LATE("T", "Tardanza"),
    ABSENT("F", "Falta"),
    JUSTIFIED("FJ", "Falta justificada"),
}

private val attendanceOptions: List<GSegmentOption<GAttendanceOption>> = GAttendanceOption.entries
    .map { GSegmentOption(it, it.label, it.contentDescription) }

@Composable
fun GAttendanceToggle(
    option: GAttendanceOption,
    isRecorded: Boolean,
    onSelect: (GAttendanceOption) -> Unit,
    modifier: Modifier = Modifier,
) {
    val outline: Color = MaterialTheme.colorScheme.outline

    GSegmentedPicker(
        options = attendanceOptions,
        selected = option.takeIf { isRecorded },
        onSelect = { value: GAttendanceOption? -> onSelect(value ?: option) },
        modifier = modifier
            .fillMaxWidth()
            .then(if (isRecorded) Modifier else Modifier.pendingOutline(outline)),
    )
}

private fun Modifier.pendingOutline(color: Color): Modifier = drawBehind {
    drawRoundRect(
        color = color,
        cornerRadius = CornerRadius(pendingCornerRadius.toPx()),
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
        GAttendanceToggle(option = GAttendanceOption.PRESENT, isRecorded = false, onSelect = {})
    }
}
