package com.emm.gema.core.domain.activity

import org.junit.Test
import java.time.LocalDate

class ActivityTest {

    @Test(expected = IllegalArgumentException::class)
    fun `an activity without a competency is rejected`() {
        Activity(
            id = "any",
            sectionId = "section",
            periodId = "period",
            name = "Debate del aula",
            date = LocalDate.of(2026, 6, 22),
            competencyIds = emptySet(),
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `an activity without a name is rejected`() {
        Activity(
            id = "any",
            sectionId = "section",
            periodId = "period",
            name = " ",
            date = LocalDate.of(2026, 6, 22),
            competencyIds = setOf("PPSS-1"),
        )
    }
}
