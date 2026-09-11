package com.emm.gema.core.domain.schoolyear

import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test

class SchoolYearIdTest {

    @Test
    fun `a blank school year id is rejected`() {
        assertThrows(IllegalArgumentException::class.java) { SchoolYearId(" ") }
    }

    @Test
    fun `a filled school year id keeps its value`() {
        assertThat(SchoolYearId("school-year-1").value).isEqualTo("school-year-1")
    }
}
