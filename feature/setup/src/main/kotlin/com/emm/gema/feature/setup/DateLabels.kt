package com.emm.gema.feature.setup

import androidx.compose.runtime.Composable
import com.emm.gema.core.theme.abbreviatedLabel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun LocalDate.asShortDayMonth(): String = "%02d %s".format(dayOfMonth, month.abbreviatedLabel())

@Composable
fun shortRangeLabel(startDate: LocalDate, endDate: LocalDate): String =
    "${startDate.asShortDayMonth()} – ${endDate.asShortDayMonth()}"

private val dayMonth: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM")

fun LocalDate.asDayMonth(): String = format(dayMonth)

fun numericRangeLabel(startDate: LocalDate, endDate: LocalDate): String =
    "${startDate.asDayMonth()} – ${endDate.asDayMonth()}"
