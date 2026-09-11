package com.emm.gema.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.emm.gema.core.theme.GemaAccents

@Composable
fun gAttendanceRowColor(isRecorded: Boolean): Color =
    if (isRecorded) Color.Transparent else GemaAccents.unmarkedSurface
