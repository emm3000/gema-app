package com.emm.gema.core.domain.schoolyear

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.LocalDate

class PeriodTest {

    @Test(expected = IllegalArgumentException::class)
    fun `a period that ends before it starts is rejected`() {
        Period(
            id = "any",
            schoolYearId = "year",
            number = 1,
            startDate = LocalDate.of(2026, 5, 15),
            endDate = LocalDate.of(2026, 3, 2),
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `a period numbered below one is rejected`() {
        Period(
            id = "any",
            schoolYearId = "year",
            number = 0,
            startDate = LocalDate.of(2026, 3, 2),
            endDate = LocalDate.of(2026, 5, 15),
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `a period without a school year is rejected`() {
        Period(
            id = "any",
            schoolYearId = " ",
            number = 1,
            startDate = LocalDate.of(2026, 3, 2),
            endDate = LocalDate.of(2026, 5, 15),
        )
    }

    @Test
    fun `a period contains the days between its dates, both included`() {
        val period = Period(
            id = "any",
            schoolYearId = "year",
            number = 1,
            startDate = LocalDate.of(2026, 3, 2),
            endDate = LocalDate.of(2026, 5, 15),
        )

        assertThat(period.contains(LocalDate.of(2026, 3, 2))).isTrue()
        assertThat(period.contains(LocalDate.of(2026, 5, 15))).isTrue()
        assertThat(period.contains(LocalDate.of(2026, 4, 1))).isTrue()
        assertThat(period.contains(LocalDate.of(2026, 3, 1))).isFalse()
        assertThat(period.contains(LocalDate.of(2026, 5, 16))).isFalse()
    }

    @Test
    fun `a bimester school year has four periods and a trimester one has three`() {
        assertThat(PeriodKind.BIMESTER.periodCount).isEqualTo(4)
        assertThat(PeriodKind.TRIMESTER.periodCount).isEqualTo(3)
    }
}
