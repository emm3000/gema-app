package com.emm.gema.home

import com.emm.gema.core.domain.date.DateNameProvider
import com.google.common.truth.Truth.assertThat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Month
import org.junit.Test

class TodayLabelTest {

    private val dateNames = object : DateNameProvider {
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
            Month.SEPTEMBER to "Setiembre",
            Month.DECEMBER to "Diciembre",
        )

        override fun weekdayName(dayOfWeek: DayOfWeek): String = weekdays.getValue(dayOfWeek)
        override fun monthName(month: Month): String = months.getValue(month)
        override fun todayPrefix(): String = "HOY"
    }

    @Test
    fun `formats a Thursday in September`() {
        assertThat(todayLabelOf(LocalDate.of(2026, 9, 10), dateNames))
            .isEqualTo("HOY · JUEVES 10 DE SETIEMBRE")
    }

    @Test
    fun `formats a Sunday`() {
        assertThat(todayLabelOf(LocalDate.of(2026, 9, 13), dateNames))
            .isEqualTo("HOY · DOMINGO 13 DE SETIEMBRE")
    }

    @Test
    fun `formats the first day of January`() {
        assertThat(todayLabelOf(LocalDate.of(2026, 1, 1), dateNames))
            .isEqualTo("HOY · JUEVES 1 DE ENERO")
    }

    @Test
    fun `formats the last day of December`() {
        assertThat(todayLabelOf(LocalDate.of(2026, 12, 31), dateNames))
            .isEqualTo("HOY · JUEVES 31 DE DICIEMBRE")
    }
}
