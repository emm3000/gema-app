package com.emm.gema.core.domain.activity

import com.emm.gema.core.domain.curriculum.CompetencyId
import com.emm.gema.core.domain.schoolyear.PeriodId
import com.emm.gema.core.domain.section.SectionId
import java.time.LocalDate
import org.junit.Test

class ActivityTest {

    @Test(expected = IllegalArgumentException::class)
    fun `an activity without a competency is rejected`() {
        Activity(
            id = ActivityId("any"),
            sectionId = SectionId("section"),
            periodId = PeriodId("period"),
            name = "Debate del aula",
            date = LocalDate.of(2026, 6, 22),
            competencyIds = emptySet(),
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `an activity without a name is rejected`() {
        Activity(
            id = ActivityId("any"),
            sectionId = SectionId("section"),
            periodId = PeriodId("period"),
            name = " ",
            date = LocalDate.of(2026, 6, 22),
            competencyIds = setOf(firstPpssId),
        )
    }
}

private val firstPpssId: CompetencyId = CompetencyId("PPSS-1")
