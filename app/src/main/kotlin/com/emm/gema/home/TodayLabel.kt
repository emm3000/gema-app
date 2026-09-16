package com.emm.gema.home

import com.emm.gema.core.domain.date.DateNameProvider
import java.time.LocalDate

internal fun todayLabelOf(date: LocalDate, dateNames: DateNameProvider): String {
    val weekday: String = dateNames.weekdayName(date.dayOfWeek).uppercase()
    val month: String = dateNames.monthName(date.month).uppercase()
    return "${dateNames.todayPrefix()} · $weekday ${date.dayOfMonth} DE $month"
}
