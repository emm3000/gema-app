package com.emm.gema.home

import com.google.common.truth.Truth.assertThat
import java.time.LocalDate
import org.junit.Test

class TodayLabelTest {

    private val dateNames: DateNameProvider = FakeDateNameProvider()

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
