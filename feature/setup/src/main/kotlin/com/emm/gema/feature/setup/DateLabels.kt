package com.emm.gema.feature.setup

import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val dayMonthYear: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

fun LocalDate.asDayMonthYear(): String = format(dayMonthYear)

fun rangeLabel(startDate: LocalDate, endDate: LocalDate): String =
    "${startDate.asDayMonthYear()} - ${endDate.asDayMonthYear()}"
