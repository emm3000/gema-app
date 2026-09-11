package com.emm.gema.core.domain.section

import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test

class SectionIdTest {

    @Test
    fun `a blank section id is rejected`() {
        assertThrows(IllegalArgumentException::class.java) { SectionId(" ") }
    }

    @Test
    fun `a filled section id keeps its value`() {
        assertThat(SectionId("section-1").value).isEqualTo("section-1")
    }
}
