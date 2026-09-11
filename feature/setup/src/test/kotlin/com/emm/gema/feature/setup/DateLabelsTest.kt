package com.emm.gema.feature.setup

import com.google.common.truth.Truth.assertThat
import java.time.LocalDate
import org.junit.Test

class DateLabelsTest {

    @Test
    fun `formats a day month year range with a hyphen`() {
        val label: String = rangeLabel(LocalDate.of(2026, 3, 1), LocalDate.of(2026, 5, 15))

        assertThat(label).isEqualTo("01/03/2026 - 15/05/2026")
    }
}
