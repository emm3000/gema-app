package com.emm.gema.feature.students

import com.emm.gema.core.domain.section.Section
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val dayMonthYear: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

fun Section.title(): String = "${grade.number}° $name"

fun LocalDate.asDayMonthYear(): String = format(dayMonthYear)
