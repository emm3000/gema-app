package com.emm.gema.feature.sections

import java.time.LocalDate

private val dayNames: List<String> = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")

private val monthNames: List<String> = listOf(
    "enero", "febrero", "marzo", "abril", "mayo", "junio",
    "julio", "agosto", "setiembre", "octubre", "noviembre", "diciembre",
)

fun LocalDate.asDayLabel(): String =
    "${dayNames[dayOfWeek.value - 1]} $dayOfMonth de ${monthNames[monthValue - 1]}"
