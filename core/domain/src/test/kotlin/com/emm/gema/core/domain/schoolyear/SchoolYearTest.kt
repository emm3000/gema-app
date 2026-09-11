package com.emm.gema.core.domain.schoolyear

import org.junit.Test
import java.time.LocalDate

class SchoolYearTest {

    @Test(expected = IllegalArgumentException::class)
    fun `a school year that ends before it starts is rejected`() {
        schoolYear(startDate = LocalDate.of(2026, 12, 18), endDate = LocalDate.of(2026, 3, 2))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `a school year without an id is rejected`() {
        schoolYear(id = " ")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `a school year without a label is rejected`() {
        schoolYear(label = "  ")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `a school year too short for its periods is rejected`() {
        schoolYear(
            startDate = LocalDate.of(2026, 3, 2),
            endDate = LocalDate.of(2026, 3, 4),
            periodKind = PeriodKind.BIMESTER,
        )
    }

    private fun schoolYear(
        id: String = "year",
        label: String = "2026",
        startDate: LocalDate = LocalDate.of(2026, 3, 2),
        endDate: LocalDate = LocalDate.of(2026, 12, 18),
        periodKind: PeriodKind = PeriodKind.BIMESTER,
    ): SchoolYear = SchoolYear(
        id = id,
        label = label,
        startDate = startDate,
        endDate = endDate,
        periodKind = periodKind,
    )
}
