package com.emm.gema.core.domain.activity

import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test

class ActivityIdTest {

    @Test
    fun `a blank activity id is rejected`() {
        assertThrows(IllegalArgumentException::class.java) { ActivityId(" ") }
    }

    @Test
    fun `a filled activity id keeps its value`() {
        assertThat(ActivityId("activity-1").value).isEqualTo("activity-1")
    }
}
