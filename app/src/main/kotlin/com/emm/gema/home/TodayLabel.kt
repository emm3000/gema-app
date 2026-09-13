package com.emm.gema.home

import java.time.LocalDate

private val weekdayNames: List<String> =
    listOf("LUNES", "MARTES", "MIÉRCOLES", "JUEVES", "VIERNES", "SÁBADO", "DOMINGO")

private val monthNames: List<String> = listOf(
    "ENERO", "FEBRERO", "MARZO", "ABRIL", "MAYO", "JUNIO",
    "JULIO", "AGOSTO", "SETIEMBRE", "OCTUBRE", "NOVIEMBRE", "DICIEMBRE",
)

internal fun todayLabelOf(date: LocalDate): String =
    "HOY · ${weekdayNames[date.dayOfWeek.value - 1]} ${date.dayOfMonth} DE ${monthNames[date.monthValue - 1]}"
