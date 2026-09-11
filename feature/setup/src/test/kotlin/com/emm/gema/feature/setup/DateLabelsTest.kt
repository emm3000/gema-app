package com.emm.gema.feature.setup

import com.google.common.truth.Truth.assertThat
import java.time.LocalDate
import org.junit.Test

class DateLabelsTest {

    @Test
    fun `formats a short day and month with leading zero`() {
        assertThat(LocalDate.of(2026, 3, 1).asShortDayMonth()).isEqualTo("01 mar")
    }

    @Test
    fun `formats a short range with an en dash`() {
        val label: String = shortRangeLabel(LocalDate.of(2026, 3, 1), LocalDate.of(2026, 5, 15))

        assertThat(label).isEqualTo("01 mar – 15 may")
    }
}
