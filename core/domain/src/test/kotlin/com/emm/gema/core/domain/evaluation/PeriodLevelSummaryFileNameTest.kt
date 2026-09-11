package com.emm.gema.core.domain.evaluation

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PeriodLevelSummaryFileNameTest {

    @Test
    fun `the name slugs the section and period and keeps the extension`() {
        val name: String = periodLevelSummaryFileName(
            sectionTitle = "3ro A",
            periodLabel = "II Bimestre",
            extension = "csv",
        )

        assertThat(name).isEqualTo("resumen-3ro-a-ii-bimestre.csv")
    }

    @Test
    fun `accents are stripped from the slug`() {
        val name: String = periodLevelSummaryFileName(
            sectionTitle = "Sección A",
            periodLabel = "I Bimestre",
            extension = "pdf",
        )

        assertThat(name).isEqualTo("resumen-seccion-a-i-bimestre.pdf")
    }
}
