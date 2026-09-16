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
    val none: Dp = 0.dp
    val extraSmall: Dp = 4.dp
    val small: Dp = 8.dp
    val rowGap: Dp = 12.dp
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
    val fabHeight: Dp = 52.dp
    val activeBorder: Dp = 2.dp
    val narrowCellWidth: Dp = 36.dp
    val compactRowHeight: Dp = 52.dp
    val evidenceChipWidth: Dp = 34.dp
    val evidenceChipHeight: Dp = 26.dp
    val buttonIconSize: Dp = 20.dp
    val eyebrowGap: Dp = 20.dp
    val compactLineHeight: Dp = 40.dp
}

object GemaShapes {
    val chip: Shape = RoundedCornerShape(6.dp)
    val controlRadius: Dp = 8.dp
    val control: CornerBasedShape = RoundedCornerShape(controlRadius)
    val container: Shape = RoundedCornerShape(12.dp)
    val pill: Shape = RoundedCornerShape(percent = 50)
}

object GemaBorder {
    val hairline: Dp = 1.dp
}

object GemaAccents {

    val warningContainer: Color
        @Composable get() = if (isSystemInDarkTheme()) warningContainerDark else warningContainerLight

    val onWarningContainer: Color
        @Composable get() = if (isSystemInDarkTheme()) onWarningContainerDark else onWarningContainerLight
}
