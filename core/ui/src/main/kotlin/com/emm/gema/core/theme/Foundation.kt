package com.emm.gema.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object GemaSpacing {
    val extraSmall: Dp = 4.dp
    val small: Dp = 8.dp
    val medium: Dp = 16.dp
    val large: Dp = 24.dp
    val extraLarge: Dp = 32.dp
    val screenGutter: Dp = 16.dp
    val minimumTouchTarget: Dp = 48.dp
    val indicatorStroke: Dp = 2.dp
    val narrowFieldWidth: Dp = 112.dp
    val gridNameColumnWidth: Dp = 150.dp
    val gridCellWidth: Dp = 56.dp
    val gridRowHeight: Dp = 56.dp
    val gridChipHeight: Dp = 44.dp
    val leadingLabelWidth: Dp = 28.dp
    val fabHeight: Dp = 56.dp
    val activeBorder: Dp = 2.dp
}

object GemaShapes {
    val control: CornerBasedShape = RoundedCornerShape(12.dp)
    val container: Shape = RoundedCornerShape(16.dp)
    val pill: Shape = RoundedCornerShape(percent = 50)
}

object GemaBorder {
    val hairline: Dp = 1.dp
}

object GemaAccents {

    val unmarkedSurface: Color
        @Composable get() = if (isSystemInDarkTheme()) unmarkedDark else unmarkedLight

    val onUnmarkedSurface: Color
        @Composable get() = if (isSystemInDarkTheme()) onUnmarkedDark else onUnmarkedLight
}
