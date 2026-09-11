package com.emm.gema.core.domain.schoolyear

import org.junit.Test
import java.time.LocalDate

class SchoolYearTest {

    @Test(expected = IllegalArgumentException::class)
    fun `a school year that ends before it starts is rejected`() {
        SchoolYear(
            id = "any",
            startDate = LocalDate.of(2026, 12, 18),
            endDate = LocalDate.of(2026, 3, 2),
            periodKind = PeriodKind.BIMESTER,
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `a school year without an id is rejected`() {
        SchoolYear(
            id = " ",
            startDate = LocalDate.of(2026, 3, 2),
            endDate = LocalDate.of(2026, 12, 18),
            periodKind = PeriodKind.BIMESTER,
        )
    }
}
