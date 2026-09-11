package com.emm.gema.feature.setup

import androidx.compose.runtime.Composable
import com.emm.gema.core.theme.abbreviatedLabel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val dayMonthYear: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

fun LocalDate.asDayMonthYear(): String = format(dayMonthYear)

@Composable
fun LocalDate.asShortDayMonth(): String = "%02d %s".format(dayOfMonth, month.abbreviatedLabel())

fun rangeLabel(startDate: LocalDate, endDate: LocalDate): String =
    "${startDate.asDayMonthYear()} - ${endDate.asDayMonthYear()}"

@Composable
fun shortRangeLabel(startDate: LocalDate, endDate: LocalDate): String =
    "${startDate.asShortDayMonth()} – ${endDate.asShortDayMonth()}"
