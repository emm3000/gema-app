package com.emm.gema.feature.attendance

import com.emm.gema.core.domain.section.Section
import java.time.LocalDate

private val dayNames: List<String> = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")

private val monthNames: List<String> = listOf(
    "ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "set", "oct", "nov", "dic",
)

fun Section.title(): String = "${grade.number}° $name"

fun LocalDate.asDayLabel(): String =
    "${dayNames[dayOfWeek.value - 1]} $dayOfMonth ${monthNames[monthValue - 1]} $year"
