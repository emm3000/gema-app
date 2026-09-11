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
import com.emm.gema.core.domain.attendance.AttendanceStatus
import com.emm.gema.core.theme.GemaBorder
import com.emm.gema.core.theme.GemaTheme

private val pendingCornerRadius: Dp = 12.dp
private val pendingDashLength: Dp = 6.dp
private val pendingDashGap: Dp = 4.dp

private val attendanceOptions: List<GSegmentOption<AttendanceStatus>> = listOf(
    GSegmentOption(AttendanceStatus.PRESENT, "P", "Presente"),
    GSegmentOption(AttendanceStatus.LATE, "T", "Tardanza"),
    GSegmentOption(AttendanceStatus.ABSENT, "F", "Falta"),
    GSegmentOption(AttendanceStatus.JUSTIFIED, "FJ", "Falta justificada"),
)

@Composable
fun GAttendanceToggle(
    status: AttendanceStatus,
    isRecorded: Boolean,
    onSelect: (AttendanceStatus) -> Unit,
    modifier: Modifier = Modifier,
) {
    val outline: Color = MaterialTheme.colorScheme.outline

    GSegmentedPicker(
        options = attendanceOptions,
        selected = status.takeIf { isRecorded },
        onSelect = { value: AttendanceStatus? -> onSelect(value ?: status) },
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
        GAttendanceToggle(status = AttendanceStatus.PRESENT, isRecorded = false, onSelect = {})
    }
}
