package com.emm.gema.feature.students

import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val dayMonthYear: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

fun LocalDate.asDayMonthYear(): String = format(dayMonthYear)
