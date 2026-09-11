package com.emm.gema.core.domain.schoolyear

import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test

class PeriodIdTest {

    @Test
    fun `a blank period id is rejected`() {
        assertThrows(IllegalArgumentException::class.java) { PeriodId(" ") }
    }

    @Test
    fun `a filled period id keeps its value`() {
        assertThat(PeriodId("period-1").value).isEqualTo("period-1")
    }
}
