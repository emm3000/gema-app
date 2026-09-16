package com.emm.gema.core.theme

import java.time.LocalDate
import java.util.Locale

fun todayLabelOf(date: LocalDate, dateNames: DateNameProvider): String {
    val weekday: String = dateNames.weekdayName(date.dayOfWeek).uppercase(Locale.ROOT)
    val month: String = dateNames.monthName(date.month).uppercase(Locale.ROOT)
    return dateNames.todayLabel(weekday, date.dayOfMonth, month)
}
