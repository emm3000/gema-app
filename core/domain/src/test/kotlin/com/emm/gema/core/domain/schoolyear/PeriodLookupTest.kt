package com.emm.gema.core.domain.schoolyear

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.LocalDate

class PeriodLookupTest {

    private val firstPeriod: Period = Period(
        id = PeriodId("period-1"),
        schoolYearId = SchoolYearId("year"),
        number = 1,
        startDate = LocalDate.of(2026, 3, 2),
        endDate = LocalDate.of(2026, 5, 10),
    )
    private val secondPeriod: Period = Period(
        id = PeriodId("period-2"),
        schoolYearId = SchoolYearId("year"),
        number = 2,
        startDate = LocalDate.of(2026, 5, 11),
        endDate = LocalDate.of(2026, 7, 24),
    )
    private val periods: List<Period> = listOf(firstPeriod, secondPeriod)

    @Test
    fun `finds the period a date falls inside`() {
        assertThat(periods.periodFor(LocalDate.of(2026, 4, 1))).isEqualTo(firstPeriod)
    }

    @Test
    fun `finds the period on its start boundary`() {
        assertThat(periods.periodFor(LocalDate.of(2026, 5, 11))).isEqualTo(secondPeriod)
    }

    @Test
    fun `finds the period on its end boundary`() {
        assertThat(periods.periodFor(LocalDate.of(2026, 5, 10))).isEqualTo(firstPeriod)
    }

    @Test
    fun `returns null for a date outside every period`() {
        assertThat(periods.periodFor(LocalDate.of(2026, 1, 1))).isNull()
    }
}
