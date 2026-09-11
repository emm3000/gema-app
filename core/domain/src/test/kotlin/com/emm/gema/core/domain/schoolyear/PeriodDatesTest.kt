package com.emm.gema.core.domain.schoolyear

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class PeriodDatesTest {

    @Test
    fun `a bimester year is divided into four equal periods`() {
        val periods: List<PeriodDates> = PeriodKind.BIMESTER.divide(
            startDate = LocalDate.of(2026, 1, 1),
            endDate = LocalDate.of(2026, 1, 12),
        )

        assertThat(periods).containsExactly(
            PeriodDates(1, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 3)),
            PeriodDates(2, LocalDate.of(2026, 1, 4), LocalDate.of(2026, 1, 6)),
            PeriodDates(3, LocalDate.of(2026, 1, 7), LocalDate.of(2026, 1, 9)),
            PeriodDates(4, LocalDate.of(2026, 1, 10), LocalDate.of(2026, 1, 12)),
        ).inOrder()
    }

    @Test
    fun `the remaining days go to the earliest periods`() {
        val periods: List<PeriodDates> = PeriodKind.BIMESTER.divide(
            startDate = LocalDate.of(2026, 1, 1),
            endDate = LocalDate.of(2026, 1, 14),
        )

        assertThat(periods.map { ChronoUnit.DAYS.between(it.startDate, it.endDate) + 1 })
            .containsExactly(4L, 4L, 3L, 3L)
            .inOrder()
    }

    @Test
    fun `a trimester year is divided into three periods that cover it without gaps`() {
        val startDate = LocalDate.of(2026, 3, 2)
        val endDate = LocalDate.of(2026, 12, 18)

        val periods: List<PeriodDates> = PeriodKind.TRIMESTER.divide(startDate, endDate)

        assertThat(periods).hasSize(3)
        assertThat(periods.first().startDate).isEqualTo(startDate)
        assertThat(periods.last().endDate).isEqualTo(endDate)
        periods.zipWithNext { current, next ->
            assertThat(next.startDate).isEqualTo(current.endDate.plusDays(1))
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `a year shorter than its period count is rejected`() {
        PeriodKind.BIMESTER.divide(startDate = LocalDate.of(2026, 1, 1), endDate = LocalDate.of(2026, 1, 2))
    }
}
