package com.emm.gema.core.domain.student

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class StudentCodeTest {

    @Test
    fun `a code of fourteen digits is accepted`() {
        assertThat(StudentCode.orNull("12345678901234")?.value).isEqualTo("12345678901234")
    }

    @Test
    fun `a shorter code is rejected`() {
        assertThat(StudentCode.orNull("1234567890123")).isNull()
    }

    @Test
    fun `a longer code is rejected`() {
        assertThat(StudentCode.orNull("123456789012345")).isNull()
    }

    @Test
    fun `a code with letters is rejected`() {
        assertThat(StudentCode.orNull("1234567890123A")).isNull()
    }

    @Test
    fun `building an invalid code fails fast`() {
        val failure: Result<StudentCode> = runCatching { StudentCode("abc") }

        assertThat(failure.exceptionOrNull()).isInstanceOf(IllegalArgumentException::class.java)
    }
}
