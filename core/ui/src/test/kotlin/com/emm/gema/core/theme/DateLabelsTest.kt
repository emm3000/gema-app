package com.emm.gema.core.theme

import com.google.common.truth.Truth.assertThat
import java.time.LocalDate
import org.junit.Test

class DateLabelsTest {

    @Test
    fun `asDayMonth formats as zero padded day and month`() {
        val date: LocalDate = LocalDate.of(2026, 3, 5)

        assertThat(date.asDayMonth()).isEqualTo("05/03")
    }

    @Test
    fun `asDayMonthYear formats as zero padded day month and year`() {
        val date: LocalDate = LocalDate.of(2026, 3, 5)

        assertThat(date.asDayMonthYear()).isEqualTo("05/03/2026")
    }

    @Test
    fun `numericRangeLabel joins both dates with an en dash`() {
        val startDate: LocalDate = LocalDate.of(2026, 3, 1)
        val endDate: LocalDate = LocalDate.of(2026, 7, 31)

        assertThat(numericRangeLabel(startDate, endDate)).isEqualTo("01/03 – 31/07")
    }
}
