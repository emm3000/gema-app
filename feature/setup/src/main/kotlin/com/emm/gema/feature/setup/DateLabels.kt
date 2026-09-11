package com.emm.gema.feature.setup

import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val dayMonthYear: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

private val shortMonthNames: List<String> = listOf(
    "ene", "feb", "mar", "abr", "may", "jun",
    "jul", "ago", "sep", "oct", "nov", "dic",
)

fun LocalDate.asDayMonthYear(): String = format(dayMonthYear)

fun LocalDate.asShortDayMonth(): String = "%02d %s".format(dayOfMonth, shortMonthNames[monthValue - 1])

fun rangeLabel(startDate: LocalDate, endDate: LocalDate): String =
    "${startDate.asDayMonthYear()} - ${endDate.asDayMonthYear()}"

fun shortRangeLabel(startDate: LocalDate, endDate: LocalDate): String =
    "${startDate.asShortDayMonth()} – ${endDate.asShortDayMonth()}"
