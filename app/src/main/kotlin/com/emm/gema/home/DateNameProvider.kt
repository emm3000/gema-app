package com.emm.gema.home

import java.time.DayOfWeek
import java.time.Month

interface DateNameProvider {
    fun weekdayName(dayOfWeek: DayOfWeek): String
    fun monthName(month: Month): String
    fun todayLabel(weekday: String, dayOfMonth: Int, month: String): String
}
