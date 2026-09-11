package com.emm.gema.core.domain.student

import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test

class StudentIdTest {

    @Test
    fun `a blank student id is rejected`() {
        assertThrows(IllegalArgumentException::class.java) { StudentId(" ") }
    }

    @Test
    fun `a filled student id keeps its value`() {
        assertThat(StudentId("student-1").value).isEqualTo("student-1")
    }
}
