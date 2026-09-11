package com.emm.gema.feature.setup

import androidx.compose.runtime.Composable
import com.emm.gema.core.theme.abbreviatedLabel
import java.time.LocalDate

@Composable
fun LocalDate.asShortDayMonth(): String = "%02d %s".format(dayOfMonth, month.abbreviatedLabel())

@Composable
fun shortRangeLabel(startDate: LocalDate, endDate: LocalDate): String =
    "${startDate.asShortDayMonth()} – ${endDate.asShortDayMonth()}"
