package com.emm.gema.core.domain.date

import java.time.DayOfWeek
import java.time.Month

interface DateNameProvider {
    fun weekdayName(dayOfWeek: DayOfWeek): String
    fun monthName(month: Month): String
    fun todayPrefix(): String
}
