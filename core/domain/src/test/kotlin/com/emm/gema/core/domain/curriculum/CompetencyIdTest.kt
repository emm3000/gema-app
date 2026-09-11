package com.emm.gema.core.domain.curriculum

import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test

class CompetencyIdTest {

    @Test
    fun `a blank competency id is rejected`() {
        assertThrows(IllegalArgumentException::class.java) { CompetencyId(" ") }
    }

    @Test
    fun `a filled competency id keeps its value`() {
        assertThat(CompetencyId("competency-1").value).isEqualTo("competency-1")
    }
}
