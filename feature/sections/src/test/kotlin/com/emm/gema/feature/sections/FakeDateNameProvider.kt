package com.emm.gema.feature.sections

import com.emm.gema.core.theme.DateNameProvider
import java.time.DayOfWeek
import java.time.Month

class FakeDateNameProvider : DateNameProvider {

    private val weekdays: Map<DayOfWeek, String> = mapOf(
        DayOfWeek.MONDAY to "Lunes",
        DayOfWeek.TUESDAY to "Martes",
        DayOfWeek.WEDNESDAY to "Miércoles",
        DayOfWeek.THURSDAY to "Jueves",
        DayOfWeek.FRIDAY to "Viernes",
        DayOfWeek.SATURDAY to "Sábado",
        DayOfWeek.SUNDAY to "Domingo",
    )

    private val months: Map<Month, String> = mapOf(
        Month.JANUARY to "Enero",
        Month.FEBRUARY to "Febrero",
        Month.MARCH to "Marzo",
        Month.APRIL to "Abril",
        Month.MAY to "Mayo",
        Month.JUNE to "Junio",
        Month.JULY to "Julio",
        Month.AUGUST to "Agosto",
        Month.SEPTEMBER to "Setiembre",
        Month.OCTOBER to "Octubre",
        Month.NOVEMBER to "Noviembre",
        Month.DECEMBER to "Diciembre",
    )

    override fun weekdayName(dayOfWeek: DayOfWeek): String = weekdays.getValue(dayOfWeek)

    override fun monthName(month: Month): String = months.getValue(month)

    override fun todayLabel(weekday: String, dayOfMonth: Int, month: String): String =
        "HOY · $weekday $dayOfMonth DE $month"
}
